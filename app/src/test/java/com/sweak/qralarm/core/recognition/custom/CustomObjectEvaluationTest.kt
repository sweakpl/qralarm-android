package com.sweak.qralarm.core.recognition.custom

import com.sweak.qralarm.core.domain.recognition.ObjectConfirmation
import org.junit.Assume.assumeTrue
import org.junit.BeforeClass
import org.junit.Test
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import java.io.File

/**
 * Ground-truth evaluation of the custom-object pipeline (geometric stage + temporal confirmation)
 * on real photographs of real products. Runs only when QRALARM_EVAL_DIR points at the data that
 * eval/fetch_*.py download; see eval/README.md. Prints a report and writes it to
 * build/reports/custom-object-eval.md.
 */
class CustomObjectEvaluationTest {

    private class Enrolled(val name: String, val group: String, val references: List<ReferenceFeatures>)

    private fun gray(file: File, maxSide: Int): Mat {
        val image = Imgcodecs.imread(file.path, Imgcodecs.IMREAD_GRAYSCALE)
        require(!image.empty()) { "cannot read $file" }
        return ReferenceBuilder.resizeToMaxSide(image, maxSide).also { image.release() }
    }

    /** Same rules the enrollment screen applies; null when enrollment would be refused. */
    private fun enroll(photos: List<File>, extractor: OrbExtractor): List<ReferenceFeatures>? {
        val shots = photos.mapNotNull { file ->
            val full = gray(file, 1280)
            val crop = ReferenceBuilder.cropToGuideBox(full)
            val built = ReferenceBuilder.build(crop, extractor, null)
            full.release(); crop.release()
            built.features.takeIf { EnrollmentRules.checkShot(it, built.sharpness) == null }
        }
        if (shots.size < EnrollmentRules.MIN_REFERENCES) return null
        val bad = EnrollmentRules.inconsistentReferences(shots).toSet()
        val kept = shots.filterIndexed { i, _ -> i !in bad }
        return kept.takeIf { it.size >= EnrollmentRules.MIN_REFERENCES }
    }

    private class Frame(val features: OrbFeatures, val gray: Mat) {
        val width get() = gray.cols()
        val height get() = gray.rows()
    }

    private fun frameFeatures(file: File, extractor: OrbExtractor): Frame {
        val frame = gray(file, ReferenceBuilder.FRAME_MAX_SIDE)
        return Frame(extractor.extract(frame), frame)
    }

    /** Photometric score of the best geometric candidate, or null when geometry already fails. */
    private fun score(verifier: GeometricVerifier, frame: Frame): Double? =
        verifier.candidates(frame.features, frame.width, frame.height, frame.gray)
            .maxByOrNull { it.inliers }?.let { it.photometricScore ?: 1.0 }

    private fun sweep(title: String, positives: List<Double?>, negatives: List<Double?>, hard: List<Double?>) {
        log("$title: threshold -> true accept / false accept / same-type false accept")
        listOf(0.0, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8).forEach { t ->
            fun rate(list: List<Double?>) = pct(list.count { it != null && it >= t }, list.size)
            log("  >= $t: ${rate(positives)} / ${rate(negatives)} / ${rate(hard)}")
        }
    }

    /** Loose limits, so every plausible candidate is recorded for offline threshold analysis. */
    private val diagnosticParams = GeometricVerifier.Params(
        minInliers = 8, minInlierRatio = 0.0, minInlierSpread = 0.0, minPhotometricScore = 0.0
    )
    private val csv = StringBuilder("set,target,test,label,frame,inliers,good,ratio,spread,coverage,photometric\n")

    private fun record(set: String, target: String, test: String, label: String, frame: Int, verifier: GeometricVerifier, f: Frame) {
        val m = verifier.candidates(f.features, f.width, f.height, f.gray).maxByOrNull { it.inliers }
        csv.append("$set,$target,$test,$label,$frame,").append(
            if (m == null) "0,0,0,0,0,0" else
                "${m.inliers},${m.goodMatches},${"%.3f".format(m.inliers.toDouble() / m.goodMatches)},${"%.3f".format(m.inlierSpread)},${"%.3f".format(m.coverage)},${"%.3f".format(m.photometricScore ?: 1.0)}"
        ).append('\n')
    }

    private val report = StringBuilder()
    private fun log(line: String) { println(line); report.appendLine(line) }

    @Test
    fun groceryStoreProducts() {
        val root = File(evalDir, "GroceryStoreDataset/dataset")
        assumeTrue(root.isDirectory)
        val classDirs = File(root, "train/Packages").listFiles()!!.flatMap { it.listFiles()!!.toList() }.sorted()
        val frameExtractor = OrbExtractor(OrbExtractor.FRAME_FEATURES)
        val referenceExtractor = OrbExtractor(OrbExtractor.REFERENCE_FEATURES)

        val enrolled = classDirs.mapNotNull { dir ->
            val photos = dir.listFiles()!!.sorted().take(EnrollmentRules.MAX_REFERENCES)
            enroll(photos, referenceExtractor)?.let { Enrolled(dir.name, dir.parentFile!!.name, it) }
        }
        val tests = classDirs.flatMap { dir ->
            File(root, "test/Packages/${dir.parentFile!!.name}/${dir.name}").listFiles().orEmpty().sorted()
                .map { Triple(dir.name, dir.parentFile!!.name, frameFeatures(it, frameExtractor)) }
        }
        log("## GroceryStoreDataset (packages): ${classDirs.size} products, ${enrolled.size} enrollable, ${tests.size} test photos")

        val positives = mutableListOf<Double?>()
        val negatives = mutableListOf<Double?>()
        val hard = mutableListOf<Double?>()
        val falseAccepts = mutableListOf<String>()
        val threshold = GeometricVerifier.Params().minPhotometricScore
        enrolled.forEach { target ->
            GeometricVerifier(target.references, diagnosticParams).use { v ->
                tests.forEachIndexed { i, (name, group, frame) ->
                    val label = if (name == target.name) "pos" else if (group == target.group) "hard" else "neg"
                    record("grocery", target.name, name, label, i, v, frame)
                }
            }
        }
        File("build/reports").mkdirs()
        File("build/reports/candidates-grocery.csv").writeText(csv.toString())
        enrolled.forEach { target ->
            GeometricVerifier(target.references).use { verifier ->
                tests.forEach { (name, group, frame) ->
                    val score = score(verifier, frame)
                    if (name == target.name) positives += score else {
                        negatives += score
                        if (group == target.group) hard += score
                        if (score != null && score >= threshold) falseAccepts += "${target.name} <- $name"
                    }
                }
            }
        }
        sweep("photometric threshold sweep (per photo)", positives, negatives, hard)
        falseAccepts.groupingBy { it }.eachCount().forEach { (pair, n) -> log("  false accept at default: $pair x$n") }
        writeReport("grocery")
    }

    @Test
    fun objectronInstances() {
        val root = File(evalDir, "objectron")
        assumeTrue(root.isDirectory)
        val frameExtractor = OrbExtractor(OrbExtractor.FRAME_FEATURES)
        val referenceExtractor = OrbExtractor(OrbExtractor.REFERENCE_FEATURES)
        val instances = root.listFiles()!!.flatMap { it.listFiles()!!.toList() }.sorted()
            .map { dir -> dir to dir.listFiles()!!.filter { it.extension == "jpg" }.sorted() }
            .filter { it.second.size >= 20 }

        class Split(val name: String, val category: String, val references: List<ReferenceFeatures>?, val test: List<Frame>)
        val splits = instances.map { (dir, frames) ->
            val enrollPart = frames.take((frames.size * 0.4).toInt())
            val photos = (0 until EnrollmentRules.MAX_REFERENCES).map {
                enrollPart[it * (enrollPart.size - 1) / (EnrollmentRules.MAX_REFERENCES - 1)]
            }
            Split(
                dir.name, dir.parentFile!!.name, enroll(photos, referenceExtractor),
                frames.drop(enrollPart.size).map { frameFeatures(it, frameExtractor) }
            )
        }
        val enrolled = splits.filter { it.references != null }
        log("## Objectron: ${splits.size} instances, ${enrolled.size} enrollable")

        enrolled.forEach { target ->
            GeometricVerifier(target.references!!, diagnosticParams).use { v ->
                splits.forEach { other ->
                    val label = if (other === target) "pos" else if (other.category == target.category) "hard" else "neg"
                    other.test.forEachIndexed { i, f -> record("objectron", target.name, other.name, label, i, v, f) }
                }
            }
        }
        File("build/reports").mkdirs()
        File("build/reports/candidates-objectron.csv").writeText(csv.toString())
        var dismissed = 0; var dismissedWithin3s = 0
        var frameTp = 0; var framePos = 0; var frameFp = 0; var frameNeg = 0
        var falseDismissals = 0; var negativeSequences = 0
        val falseDismissalPairs = mutableListOf<String>()
        enrolled.forEach { target ->
            CustomObjectRecognizer(target.references!!, 0f).use { recognizer ->
                splits.forEach { other ->
                    val confirmation = ObjectConfirmation(ObjectConfirmation.CUSTOM_OBJECT_POLICY)
                    var firedAt: Int? = null
                    other.test.forEachIndexed { i, frame ->
                        val verified = recognizer.evaluate(frame.features, frame.width, frame.height, frame.gray).verified
                        if (other === target) { framePos++; if (verified) frameTp++ } else { frameNeg++; if (verified) frameFp++ }
                        if (firedAt == null && confirmation.observe(if (verified) 1f else null, i * FRAME_INTERVAL_MS)) firedAt = i
                    }
                    if (other === target) {
                        if (firedAt != null) { dismissed++; if (firedAt!! * FRAME_INTERVAL_MS <= 3000) dismissedWithin3s++ }
                    } else {
                        negativeSequences++
                        if (firedAt != null) { falseDismissals++; falseDismissalPairs += "${target.name} <- ${other.name}" }
                    }
                }
            }
        }
        log("sequence true dismissal: $dismissed/${enrolled.size} (${pct(dismissed, enrolled.size)}), within 3 s: $dismissedWithin3s")
        log("sequence false dismissal: $falseDismissals/$negativeSequences (${pct(falseDismissals, negativeSequences)})")
        falseDismissalPairs.forEach { log("  false dismissal: $it") }
        log("per-frame true accept: $frameTp/$framePos (${pct(frameTp, framePos)}), false accept: $frameFp/$frameNeg (${pct(frameFp, frameNeg)})")
        writeReport("objectron")
    }

    /**
     * The user's own items (eval/extract_frames.py): every negative clip must not dismiss, and
     * at least 95% of positive clips must dismiss within 3 seconds.
     */
    @Test
    fun userCapturedItems() {
        val root = File(evalDir, "user")
        val manifest = File(root, "manifest.csv")
        assumeTrue(manifest.isFile)
        val frameExtractor = OrbExtractor(OrbExtractor.FRAME_FEATURES)
        val referenceExtractor = OrbExtractor(OrbExtractor.REFERENCE_FEATURES)
        val rows = manifest.readLines().drop(1).filter { it.isNotBlank() }.map { it.split(",") }
        val items = rows.map { it[1] }.distinct().associateWith { item ->
            enroll(File(root, "$item/enroll").listFiles().orEmpty().sorted(), referenceExtractor)
        }
        items.filterValues { it == null }.keys.forEach { log("item '$it' could not be enrolled") }
        val clips = HashMap<String, List<Frame>>()
        var positives = 0; var dismissedFast = 0; var negatives = 0; var falseDismissals = 0
        log("## Your items: ${items.size} items, ${rows.map { it[0] }.distinct().size} clips")
        rows.forEach { (clip, item, expected) ->
            val references = items[item] ?: return@forEach
            val frames = clips.getOrPut(clip) {
                File(root, clip).listFiles { f -> f.extension == "jpg" }.orEmpty().sorted()
                    .map { frameFeatures(it, frameExtractor) }
            }
            val firedAt = CustomObjectRecognizer(references, 0f).use { recognizer ->
                val confirmation = ObjectConfirmation(ObjectConfirmation.CUSTOM_OBJECT_POLICY)
                frames.indices.firstOrNull { i ->
                    val verified = recognizer.evaluate(frames[i].features, frames[i].width, frames[i].height, frames[i].gray).verified
                    confirmation.observe(if (verified) 1f else null, i * FRAME_INTERVAL_MS)
                }
            }
            if (expected == "match") {
                positives++
                if (firedAt != null && firedAt * FRAME_INTERVAL_MS <= 3000) dismissedFast++
                else log("  missed: $clip (item $item)")
            } else {
                negatives++
                if (firedAt != null) { falseDismissals++; log("  FALSE DISMISSAL: $clip dismissed $item") }
            }
        }
        log("true dismissal within 3 s: $dismissedFast/$positives (${pct(dismissedFast, positives)})")
        log("false dismissal: $falseDismissals/$negatives")
        writeReport("user")
        org.junit.Assert.assertEquals("negative clips that dismissed an alarm", 0, falseDismissals)
        org.junit.Assert.assertTrue("true dismissal rate below 95%", dismissedFast >= 0.95 * positives)
    }

    private fun pct(a: Int, b: Int) = if (b == 0) "n/a" else "%.2f%%".format(100.0 * a / b)

    private fun writeReport(name: String) {
        File("build/reports").mkdirs()
        File("build/reports/custom-object-eval-$name.md").writeText(report.toString())
    }

    companion object {
        /** Frames were extracted at 6 per second. */
        const val FRAME_INTERVAL_MS = 167L
        val evalDir: File? = System.getenv("QRALARM_EVAL_DIR")?.let(::File)

        @BeforeClass @JvmStatic
        fun loadOpenCv() {
            assumeTrue(evalDir?.isDirectory == true)
            nu.pattern.OpenCV.loadLocally()
        }
    }
}
