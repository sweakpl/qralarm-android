# Custom-object recognition: evaluation results

Thresholds in `GeometricVerifier.Params` were chosen from these runs (2026-11-18). Per-candidate
match features were dumped by `CustomObjectEvaluationTest` and swept offline; the chosen point
is the strictest one that kept most true views while producing zero false dismissals.

Chosen: min inliers 25, inlier ratio ≥ 0.20, inlier spread ≥ 0.12, coverage ≥ 0.05,
photometric score ≥ 0.40, then 4 verified frames in a row (≥ 400 ms).

| Set | Measure | Result |
| --- | --- | --- |
| Objectron, 40 enrolled instances (bottle, cereal box, book, cup) | False dismissals, other instances' clips (1,880 sequences) | **0** |
| | False accepts, single frames (58,323) | **0** |
| | True dismissal (later, partly unseen sides of the object) | 65% of clips |
| GroceryStoreDataset, 24 enrolled products | False accepts, other brands, single photos | 0.01% |
| | False accepts, same brand / other variant, single photos | 3.1% |
| | True accepts, single in-shop photos (348 px) | 39% |

What this means:
- A different object of the same kind (another bottle, another box) did not dismiss an alarm in
  any test. Objectron negatives never exceeded 20 matched points; true frames had a median of 94.
- The weak spot is a sibling product of the same brand whose packaging differs only in a few
  words or colours. Single photos were accepted 3% of the time; the app additionally needs four
  matching frames in a row, which still photos cannot measure.
- Recall on these sets is pessimistic: the grocery photos are small and taken at a distance in
  shops, and Objectron test frames include sides that were never photographed at enrollment.
  The user-captured set (`userCapturedItems`, see README) measures the real use case and
  enforces ≥ 95% true dismissal within 3 s.

Appearance (embedding) pre-filter, from `embedding_gate.py`: passes ~100% of true frames; it
removes 72% of other-object frames on Objectron and almost none on the grocery set, so it only
saves computation.
