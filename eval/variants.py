"""Fast prototype of recognition variants on the ground-truth sets (Python OpenCV).

Writes one CSV row per (target, test frame) with the best reference's match statistics for
every variant, so thresholds and confirmation rules can be swept without recomputing.
The winning variant is then ported to the app and confirmed with CustomObjectEvaluationTest.

Usage: python -I eval/variants.py <eval_dir> <out_csv> <variant> [<variant> ...]
Variants: orb-H, sift-H, sift-HP (with photometric check), sift-H10, sift-F (AKAZE is not in OpenCV 5 main)
"""
import csv
import sys
from multiprocessing import Pool
from pathlib import Path

import cv2
import numpy as np

GUIDE = 0.7
REF_MAX, FRAME_MAX = 480, 640
NEG_STRIDE = 4
CACHE = None  # set in main: <eval_dir>/cache


def load_gray(path, max_side):
    img = cv2.imread(str(path), cv2.IMREAD_GRAYSCALE)
    s = max_side / max(img.shape)
    return cv2.resize(img, None, fx=s, fy=s, interpolation=cv2.INTER_AREA) if s < 1 else img


def crop(img):
    h, w = img.shape
    side = round(min(h, w) * GUIDE)
    y, x = (h - side) // 2, (w - side) // 2
    c = img[y:y + side, x:x + side]
    s = REF_MAX / side
    return cv2.resize(c, None, fx=s, fy=s, interpolation=cv2.INTER_AREA) if s < 1 else c


def detector(name, n):
    if name == "orb":
        return cv2.ORB_create(n, 1.2, 8, 31, 0, 2, cv2.ORB_HARRIS_SCORE, 31, 15)
    if name == "sift":
        return cv2.SIFT_create(nfeatures=n, contrastThreshold=0.02)
    if name == "akaze":
        return cv2.AKAZE_create(threshold=0.0005)


def features(det, img):
    kp, des = det.detectAndCompute(img, None)
    if des is None:
        return np.zeros((0, 2), np.float32), None
    if des.dtype == np.float32:  # RootSIFT
        des = np.sqrt(des / (des.sum(axis=1, keepdims=True) + 1e-7))
    return np.array([k.pt for k in kp], np.float32), des


def matcher(des):
    if des.dtype == np.uint8:
        m = cv2.BFMatcher(cv2.NORM_HAMMING)
    else:
        m = cv2.FlannBasedMatcher(dict(algorithm=1, trees=4), dict(checks=48))
    m.add([des])
    m.train()
    return m


def cached_frame(feat, det, path):
    import hashlib
    key = CACHE / feat / (hashlib.sha1(str(path).encode()).hexdigest() + ".npz")
    if key.exists():
        z = np.load(key)
        des = z["des"]
        return z["pts"], (des if des.size else None)
    pts, des = features(det, load_gray(path, FRAME_MAX))
    key.parent.mkdir(parents=True, exist_ok=True)
    np.savez(key, pts=pts, des=des if des is not None else np.zeros((0, 0), np.uint8))
    return pts, des


THUMB, GRID, MIN_STD, CELL_NCC = 192, 6, 10.0, 0.45


def photometric(ref_img, H, frame_img):
    """Same as the app's PhotometricCheck: share of textured reference cells matching the warp."""
    h, w = ref_img.shape
    s = THUMB / max(h, w)
    tw, th = round(w * s), round(h * s)
    thumb = cv2.GaussianBlur(cv2.resize(ref_img, (tw, th), interpolation=cv2.INTER_AREA), (3, 3), 0)
    S = np.diag([w / tw, h / th, 1.0])
    M = H @ S
    warped = cv2.warpPerspective(frame_img, M, (tw, th), flags=cv2.INTER_LINEAR | cv2.WARP_INVERSE_MAP)
    valid = cv2.warpPerspective(np.full_like(frame_img, 255), M, (tw, th), flags=cv2.INTER_NEAREST | cv2.WARP_INVERSE_MAP)
    warped = cv2.GaussianBlur(warped, (3, 3), 0)
    cw, ch = tw // GRID, th // GRID
    textured = matched = 0
    for gy in range(GRID):
        for gx in range(GRID):
            rc = thumb[gy * ch:(gy + 1) * ch, gx * cw:(gx + 1) * cw]
            if rc.std() < MIN_STD:
                continue
            textured += 1
            if (valid[gy * ch:(gy + 1) * ch, gx * cw:(gx + 1) * cw] > 0).mean() < 0.9:
                continue
            fc = warped[gy * ch:(gy + 1) * ch, gx * cw:(gx + 1) * cw]
            if cv2.matchTemplate(fc, rc, cv2.TM_CCOEFF_NORMED)[0, 0] >= CELL_NCC:
                matched += 1
    return matched / textured if textured else 0.0


def evaluate(frame, refs, geometry, frame_img=None):
    """Best reference: (inliers, good, spread, coverage, photometric)."""
    fpts, fdes = frame
    best = (0, 0, 0.0, 0.0, 0.0)
    if fdes is None or len(fdes) < 12:
        return best
    for rpts, rdes, rarea, m, rimg in refs:
        knn = m.knnMatch(fdes, k=2)
        good = {}
        for pair in knn:
            if len(pair) == 2 and pair[0].distance < 0.75 * pair[1].distance:
                g = pair[0]
                if g.trainIdx not in good or g.distance < good[g.trainIdx].distance:
                    good[g.trainIdx] = g
        if len(good) < 12:
            continue
        g = list(good.values())
        src = rpts[[x.trainIdx for x in g]]
        dst = fpts[[x.queryIdx for x in g]]
        if geometry == "F":
            M, mask = cv2.findFundamentalMat(src, dst, cv2.USAC_MAGSAC, 1.5, 0.999, 5000)
        else:
            thr = 10.0 if geometry == "H10" else 5.0  # "HP": homography + photometric
            M, mask = cv2.findHomography(src, dst, cv2.USAC_MAGSAC, thr, maxIters=5000, confidence=0.999)
        if M is None or mask is None:
            continue
        inl = src[mask.ravel().astype(bool)]
        if len(inl) < 3:
            continue
        spread = cv2.contourArea(cv2.convexHull(inl)) / max(rarea, 1.0)
        photo = photometric(rimg, M, frame_img) if (geometry == "HP" and frame_img is not None and len(inl) >= 12) else 0.0
        res = (len(inl), len(g), spread, len(inl) / len(rpts), photo)
        if res[0] > best[0]:
            best = res
    return best


def prepare_refs(det, files):
    refs = []
    for f in files:
        c = crop(load_gray(f, 1280))
        pts, des = features(det, c)
        area = cv2.contourArea(cv2.convexHull(pts)) if len(pts) >= 3 else 1.0
        if len(pts) >= 150:  # enrollment detail rule
            refs.append((pts, des, area, matcher(des), c))
    return refs


def job(args):
    variant, target, ref_files, tests, cache_dir = args
    global CACHE
    CACHE = cache_dir
    feat, geom = variant.split("-")
    det = detector(feat, 600)
    fdet = detector(feat, 1000)
    refs = prepare_refs(det, ref_files)
    if len(refs) < 3:
        return []
    rows = []
    for test_name, label, frame_idx, path in tests:
        img = load_gray(path, FRAME_MAX) if geom == "HP" else None
        r = evaluate(cached_frame(feat, fdet, path), refs, geom, img)
        rows.append([variant, target, test_name, label, frame_idx, *r])
    return rows


def objectron_jobs(root, variant):
    inst = []
    for d in sorted((root / "objectron").glob("*/*")):
        fr = sorted(d.glob("*.jpg"))
        if len(fr) >= 20:
            inst.append((d.parent.name, d.name, fr))
    jobs = []
    for cat, name, fr in inst:
        enroll_idx = [round(i * (len(fr) - 1) / 7) for i in range(8)]
        near = {j for i in enroll_idx for j in range(i - 2, i + 3)}
        tests = [(name, "pos", i, f) for i, f in enumerate(fr) if i not in near]
        for c2, n2, fr2 in inst:
            if n2 != name:
                lab = "hard" if c2 == cat else "neg"
                tests += [(n2, lab, i, f) for i, f in enumerate(fr2) if i % NEG_STRIDE == 0]
        jobs.append((variant, name, [fr[i] for i in enroll_idx], tests, root / "cache"))
    return jobs


def grocery_jobs(root, variant):
    base = root / "GroceryStoreDataset/dataset"
    classes = sorted((base / "train/Packages").glob("*/*"))
    tests_all = [(c.name, c.parent.name, sorted((base / "test/Packages" / c.parent.name / c.name).glob("*.jpg"))) for c in classes]
    jobs = []
    for c in classes:
        tests = []
        for name, group, files in tests_all:
            lab = "pos" if name == c.name else ("hard" if group == c.parent.name else "neg")
            tests += [(name, lab, i, f) for i, f in enumerate(files)]
        jobs.append((variant, "grocery:" + c.name, sorted(c.glob("*.jpg"))[:5], tests, root / "cache"))
    return jobs


def _warm(args):
    feat, path, cache_dir = args
    global CACHE
    CACHE = cache_dir
    cached_frame(feat, detector(feat, 1000), path)


def main():
    root, out = Path(sys.argv[1]), Path(sys.argv[2])
    jobs = []
    for v in sys.argv[3:]:
        jobs += objectron_jobs(root, v) + grocery_jobs(root, v)
    # Fill the frame-feature cache once, in parallel, before matching.
    feats = sorted({v.split("-")[0] for v in sys.argv[3:]})
    frames = sorted({t[3] for j in jobs for t in j[3]}, key=str)
    with Pool(4) as pool:
        pool.map(_warm, [(f, p, root / "cache") for f in feats for p in frames], chunksize=64)
    with Pool(4) as pool, open(out, "w", newline="") as f:
        w = csv.writer(f)
        w.writerow(["variant", "target", "test", "label", "frame", "inliers", "good", "spread", "coverage", "photometric"])
        for i, rows in enumerate(pool.imap_unordered(job, jobs)):
            w.writerows(rows)
            f.flush()
            print(f"{i + 1}/{len(jobs)}", flush=True)


if __name__ == "__main__":
    main()
