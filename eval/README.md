# Custom-object recognition: ground-truth evaluation

The "Show my own item" dismissal must not stop an alarm for the wrong object. These scripts
download real, labelled photos and videos, and `CustomObjectEvaluationTest` (a JVM unit test)
runs the app's own recognition code over them: enrollment rules, ORB + RANSAC geometry,
photometric check and the multi-frame confirmation. Reports go to
`app/build/reports/custom-object-eval-*.md`.

| Set | What it is | Hard negatives |
| --- | --- | --- |
| GroceryStoreDataset (MIT) | 31 packaged products (juice, milk, yoghurt) photographed in shops on a phone | Same brand, other variant (Arla milk vs. Arla lactose-free milk) |
| Google Objectron (CC-BY 4.0) | Walk-around videos of single bottles, cereal boxes, books, cups | Other instances of the same category |
| Your own items | Your medication bottles, face wash, etc. (see below) | Other brands, variants, cap/back only, empty rooms |

## Running

```bash
python3 -m venv ~/eval-venv && ~/eval-venv/bin/pip install opencv-python-headless numpy mediapipe
git clone --depth 1 https://github.com/marcusklasson/GroceryStoreDataset <eval_dir>/GroceryStoreDataset
~/eval-venv/bin/python -I eval/fetch_objectron.py <eval_dir> 12
QRALARM_EVAL_DIR=<eval_dir> devenv shell -- test --tests '*CustomObjectEvaluationTest*'
```

`eval/embedding_gate.py <eval_dir> <repo_dir>` evaluates the appearance pre-filter (MediaPipe has
no JVM build, so this part runs in Python) and regenerates
`app/src/main/assets/generic-scene-embeddings.bin`. It also expects the embedder model at
`<eval_dir>/models/mobilenet_v3_small.tflite` (same file as the app asset).

## Adding your own items

Record with your phone, the way you would hold things when the alarm rings:

```
<in>/<item>/enroll/*.jpg     3-5 photos, label filling the centre square
<in>/<item>/positive/*.mp4   a few short videos showing the item to the camera
<in>/negatives/*.mp4         another brand, another variant, only the cap, only the back,
                             the empty bathroom / kitchen / bedside table
```

```bash
~/eval-venv/bin/python -I eval/extract_frames.py <in> <eval_dir>
QRALARM_EVAL_DIR=<eval_dir> devenv shell -- test --tests '*CustomObjectEvaluationTest.userCapturedItems'
```

Ship criteria checked by that test: no negative clip dismisses an alarm, and at least 95% of
positive clips dismiss it within 3 seconds.
