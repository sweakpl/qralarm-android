"""Download a fixed subset of Google Objectron (CC-BY 4.0) and extract frames.

Each video shows one physical object instance filmed while walking around it. Other instances of
the same category are the hard negatives. Frames are written as
<out>/objectron/<category>/<instance>/frame_<n>.jpg, scaled so the long side is 640 px, matching
what the phone camera analysis stream delivers.

Usage: python -I eval/fetch_objectron.py <out_dir> [instances_per_category]
"""
import sys
import urllib.request
from pathlib import Path

import cv2

CATEGORIES = ["bottle", "cereal_box", "book", "cup"]
BASE = "https://storage.googleapis.com/objectron"
FRAMES_PER_SECOND = 6


def main():
    out = Path(sys.argv[1]) / "objectron"
    per_category = int(sys.argv[2]) if len(sys.argv) > 2 else 12
    for category in CATEGORIES:
        index = urllib.request.urlopen(f"{BASE}/v1/index/{category}_annotations_test", timeout=60)
        # The index order is fixed, so the first N test instances are a reproducible subset.
        instances = index.read().decode().split()[:per_category]
        for instance in instances:
            target = out / category / instance.replace("/", "_")
            if target.exists() and any(target.iterdir()):
                print(f"have {target}")
                continue
            target.mkdir(parents=True, exist_ok=True)
            video = target / "video.MOV"
            urllib.request.urlretrieve(f"{BASE}/videos/{instance}/video.MOV", video)
            capture = cv2.VideoCapture(str(video))
            fps = capture.get(cv2.CAP_PROP_FPS) or 30
            step = max(1, round(fps / FRAMES_PER_SECOND))
            n = written = 0
            while True:
                ok, frame = capture.read()
                if not ok:
                    break
                if n % step == 0:
                    h, w = frame.shape[:2]
                    scale = 640 / max(h, w)
                    frame = cv2.resize(frame, (round(w * scale), round(h * scale)), interpolation=cv2.INTER_AREA)
                    cv2.imwrite(str(target / f"frame_{written:04d}.jpg"), frame, [cv2.IMWRITE_JPEG_QUALITY, 90])
                    written += 1
                n += 1
            capture.release()
            video.unlink()
            print(f"{target}: {written} frames", flush=True)


if __name__ == "__main__":
    main()
