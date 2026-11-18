"""Reproduce the bundled assets without trusting a mutable download."""
from pathlib import Path
import hashlib
import urllib.request

assets = Path("app/src/main/assets")
assets.mkdir(parents=True, exist_ok=True)
downloads = {
    "efficientdet-lite0.tflite": (
        "https://storage.googleapis.com/mediapipe-models/object_detector/efficientdet_lite0/int8/1/efficientdet_lite0.tflite",
        "0720bf247bd76e6594ea28fa9c6f7c5242be774818997dbbeffc4da460c723bb",
    ),
    "mobilenet-v3-small-embedder.tflite": (
        "https://storage.googleapis.com/mediapipe-models/image_embedder/mobilenet_v3_small/float32/1/mobilenet_v3_small.tflite",
        "bbbb4c51a55a53905af1daec995ca1aae355046f8839bb8c9f5ce9271394bc40",
    ),
    "object-labels.txt": (
        "https://storage.googleapis.com/mediapipe-tasks/object_detector/labelmap.txt",
        "f8803ef7900160c629d570848dfda4175e21667bf7b71f73f8ece4938c9f2bf2",
    ),
}
for name, (url, expected) in downloads.items():
    destination = assets / name
    if destination.exists() and hashlib.sha256(destination.read_bytes()).hexdigest() == expected:
        print(f"Verified {name}")
        continue
    with urllib.request.urlopen(url, timeout=60) as response:
        data = response.read()
    if hashlib.sha256(data).hexdigest() != expected:
        raise RuntimeError(f"Checksum mismatch for {name}; existing asset was not changed")
    temporary = destination.with_suffix(".download")
    temporary.write_bytes(data)
    temporary.replace(destination)
    print(f"Downloaded and verified {name}")
