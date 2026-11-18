"""Turn your own phone videos into the frame folders the evaluation reads.

Expected input (record with the phone camera, portrait, roughly as you would at alarm time):

  <in>/<item>/enroll/*.jpg          3-5 photos with the label inside the centre square
  <in>/<item>/positive/*.mp4        videos of that item being shown to the camera
  <in>/negatives/*.mp4              hard negatives: another brand, another variant of the same
                                    brand, only the cap, only the back, empty bathroom/kitchen...

Output: <out>/user/... with every video replaced by a folder of frames at 6 fps (long side 640 px)
and a manifest.csv listing each clip, the item it is tested against, and match / no_match.
Every negative clip is tested against every item.

Usage: python -I eval/extract_frames.py <in_dir> <eval_dir>
"""
import csv
import shutil
import sys
from pathlib import Path

import cv2

FPS = 6
VIDEO_EXTENSIONS = {".mp4", ".mov", ".3gp", ".mkv"}


def extract(video, target):
    target.mkdir(parents=True, exist_ok=True)
    capture = cv2.VideoCapture(str(video))
    fps = capture.get(cv2.CAP_PROP_FPS) or 30
    step = max(1, round(fps / FPS))
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
    return written


def main():
    source, out = Path(sys.argv[1]), Path(sys.argv[2]) / "user"
    out.mkdir(parents=True, exist_ok=True)
    rows = []
    items = sorted(d for d in source.iterdir() if d.is_dir() and d.name != "negatives")
    for item in items:
        enroll = out / item.name / "enroll"
        enroll.mkdir(parents=True, exist_ok=True)
        for photo in sorted((item / "enroll").glob("*.jp*g")):
            shutil.copy(photo, enroll / photo.name)
        for video in sorted((item / "positive").iterdir()):
            if video.suffix.lower() in VIDEO_EXTENSIONS:
                clip = out / item.name / "positive" / video.stem
                print(f"{clip}: {extract(video, clip)} frames")
                rows.append((clip.relative_to(out), item.name, "match"))
    for video in sorted((source / "negatives").iterdir()) if (source / "negatives").is_dir() else []:
        if video.suffix.lower() in VIDEO_EXTENSIONS:
            clip = out / "negatives" / video.stem
            print(f"{clip}: {extract(video, clip)} frames")
            rows += [(clip.relative_to(out), item.name, "no_match") for item in items]
    # Each item's positive clips are also negatives for every other item.
    rows += [(clip, other.name, "no_match") for clip, item, label in list(rows) if label == "match"
             for other in items if other.name != item]
    with open(out / "manifest.csv", "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["clip", "item", "expected"])
        writer.writerows(rows)
    print(f"wrote {out / 'manifest.csv'} ({len(rows)} rows)")


if __name__ == "__main__":
    main()
