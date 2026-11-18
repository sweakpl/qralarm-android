"""Calibrate and evaluate the appearance (embedding) gate, and build the bundled scene asset.

1. Downloads a fixed list of generic COCO scenes (first N files of test2017, sorted by name).
2. Embeds them with the same MobileNetV3-small embedder the app uses, and writes
   app/src/main/assets/generic-scene-embeddings.bin (int32 count, int32 dim, float32 values).
3. For every product/instance in the ground-truth sets, applies the app's threshold rule
   (EnrollmentRules.embeddingThreshold) and reports how many true and false frames the gate passes.

Usage: python -I eval/embedding_gate.py <eval_dir> <repo_dir> [scene_count]
"""
import io
import struct
import sys
import urllib.request
import zipfile
import json
from pathlib import Path

import cv2
import mediapipe as mp
import numpy as np
from mediapipe.tasks.python import BaseOptions, vision

GUIDE_BOX_FRACTION = 0.7
GATE_MARGIN, GATE_MIN, GATE_MAX = 0.02, 0.20, 0.60


def embedder(model):
    return vision.ImageEmbedder.create_from_options(vision.ImageEmbedderOptions(
        base_options=BaseOptions(model_asset_path=str(model), delegate=BaseOptions.Delegate.CPU),
        l2_normalize=True))


def embed(e, bgr):
    rgb = cv2.cvtColor(bgr, cv2.COLOR_BGR2RGB)
    image = mp.Image(image_format=mp.ImageFormat.SRGB, data=np.ascontiguousarray(rgb))
    return np.array(e.embed(image).embeddings[0].embedding, dtype=np.float32)


def guide_box(bgr):
    h, w = bgr.shape[:2]
    side = round(min(h, w) * GUIDE_BOX_FRACTION)
    y, x = (h - side) // 2, (w - side) // 2
    return bgr[y:y + side, x:x + side]


def threshold(refs, scenes):
    p95 = max(float(np.sort(scenes @ r)[int((len(scenes) - 1) * 0.95)]) for r in refs)
    return min(max(p95 + GATE_MARGIN, GATE_MIN), GATE_MAX)


def fetch_scenes(out, count):
    out.mkdir(parents=True, exist_ok=True)
    files = sorted(out.glob("*.jpg"))
    if len(files) >= count:
        return files[:count]
    info = urllib.request.urlopen("http://images.cocodataset.org/annotations/image_info_test2017.zip", timeout=120).read()
    with zipfile.ZipFile(io.BytesIO(info)) as z:
        names = sorted(i["file_name"] for i in json.loads(z.read(z.namelist()[0]))["images"])[:count]
    for name in names:
        target = out / name
        if not target.exists():
            target.write_bytes(urllib.request.urlopen(f"http://images.cocodataset.org/test2017/{name}", timeout=60).read())
    return sorted(out.glob("*.jpg"))[:count]


def main():
    eval_dir, repo = Path(sys.argv[1]), Path(sys.argv[2])
    count = int(sys.argv[3]) if len(sys.argv) > 3 else 300
    e = embedder(eval_dir / "models" / "mobilenet_v3_small.tflite")
    scenes = np.stack([embed(e, cv2.imread(str(f))) for f in fetch_scenes(eval_dir / "coco-scenes", count)])
    asset = repo / "app/src/main/assets/generic-scene-embeddings.bin"
    with open(asset, "wb") as f:
        f.write(struct.pack(">ii", *scenes.shape))
        f.write(scenes.astype(">f4").tobytes())
    print(f"wrote {asset} ({scenes.shape[0]} scenes, dim {scenes.shape[1]})")

    def evaluate(title, objects):
        """objects: list of (name, reference_files, {test_name: [files]})."""
        tp = pos = fp = neg = 0
        thresholds = []
        frame_cache = {}

        def frame_embeddings(f):
            if f not in frame_cache:
                img = cv2.imread(str(f))
                frame_cache[f] = [embed(e, img), embed(e, guide_box(img))]
            return frame_cache[f]

        for name, ref_files, tests in objects:
            refs = [embed(e, guide_box(cv2.imread(str(f)))) for f in ref_files]
            t = threshold(refs, scenes)
            thresholds.append(t)
            for test_name, files in tests.items():
                for f in files:
                    sim = max(float(r @ v) for r in refs for v in frame_embeddings(f))
                    if test_name == name:
                        pos += 1; tp += sim >= t
                    else:
                        neg += 1; fp += sim >= t
        print(f"## {title}: gate thresholds {min(thresholds):.2f}..{max(thresholds):.2f} (median {np.median(thresholds):.2f})")
        print(f"gate passes true frames {tp}/{pos} ({100 * tp / max(pos, 1):.1f}%), "
              f"other objects' frames {fp}/{neg} ({100 * fp / max(neg, 1):.1f}%)")

    grocery = eval_dir / "GroceryStoreDataset/dataset"
    if grocery.is_dir():
        classes = sorted(p for p in (grocery / "train/Packages").glob("*/*"))
        tests = {c.name: sorted((grocery / "test/Packages" / c.parent.name / c.name).glob("*.jpg")) for c in classes}
        evaluate("GroceryStoreDataset", [(c.name, sorted(c.glob("*.jpg"))[:5], tests) for c in classes])

    objectron = eval_dir / "objectron"
    if objectron.is_dir():
        objs, tests = [], {}
        for d in sorted(objectron.glob("*/*")):
            frames = sorted(d.glob("*.jpg"))
            if len(frames) < 20:
                continue
            k = int(len(frames) * 0.4)
            refs = [frames[i * (k - 1) // 4] for i in range(5)]
            tests[d.name] = frames[k:]
            objs.append((d.name, refs))
        evaluate("Objectron", [(n, r, tests) for n, r in objs])
    e.close()


if __name__ == "__main__":
    main()
