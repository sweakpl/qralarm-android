"""Sweep thresholds and confirmation rules over eval/variants.py output.

For every variant and threshold, reports on Objectron (whole-clip enrollment protocol):
true dismissal within 3 s (all / bottles+cups), false-accepted frames and false dismissals of
other instances; and on GroceryStoreDataset, per-photo accepts (true / same-brand / other).

Usage: python -I eval/analyze_variants.py <variants.csv>
"""
import collections
import csv
import itertools
import sys

FRAME_MS = 167


def confirms(flags, required, window, span_ms=400):
    recent = collections.deque()
    for i, ok in enumerate(flags):
        if window == required and not ok:
            recent.clear()
            continue
        recent.append(i if ok else None)
        while len(recent) > window:
            recent.popleft()
        while recent and recent[0] is None:
            recent.popleft()
        if ok and sum(x is not None for x in recent) >= required and (i - recent[0]) * FRAME_MS >= span_ms:
            return i
    return None


def main():
    data = collections.defaultdict(list)
    for r in csv.DictReader(open(sys.argv[1])):
        data[r["variant"]].append(r)
    print("variant | rule | minInl spread cov | Objectron TD<=3s all / bottle+cup | frame FA | seq FD | grocery pos / same-brand / other")
    for variant, rows in sorted(data.items()):
        for r in rows:
            r["inliers"] = int(r["inliers"]); r["spread"] = float(r["spread"]); r["coverage"] = float(r["coverage"])
        obj = [r for r in rows if not r["target"].startswith("grocery:")]
        gro = [r for r in rows if r["target"].startswith("grocery:")]
        seqs = collections.defaultdict(list)
        for r in obj:
            seqs[(r["target"], r["test"], r["label"])].append(r)
        for v in seqs.values():
            v.sort(key=lambda r: int(r["frame"]))
        results = []
        for mi, sp, cov, (req, win) in itertools.product(
                [15, 20, 25, 30, 40, 50], [0.0, 0.12, 0.25], [0.0, 0.05], [(4, 4), (4, 6), (3, 5)]):
            ok = lambda r: r["inliers"] >= mi and r["spread"] >= sp and r["coverage"] >= cov
            td = tdc = npos = nposc = fd = 0
            for (t, test, lab), v in seqs.items():
                fired = confirms([ok(r) for r in v], req, win)
                if lab == "pos":
                    curved = t.startswith(("bottle", "cup"))
                    npos += 1; nposc += curved
                    hit = fired is not None and fired * FRAME_MS <= 3000
                    td += hit; tdc += hit and curved
                elif fired is not None:
                    fd += 1
            ffa = sum(ok(r) for r in obj if r["label"] != "pos")
            g = lambda lab: sum(ok(r) for r in gro if r["label"] == lab) / max(1, sum(r["label"] == lab for r in gro))
            results.append(((req, win), (mi, sp, cov), td / max(npos, 1), tdc / max(nposc, 1), ffa, fd, g("pos"), g("hard"), g("neg")))
        safe = [x for x in results if x[4] == 0 and x[5] == 0]
        for x in sorted(safe, key=lambda x: (-x[2], x[7]))[:5]:
            print(variant, x[0], x[1], f"{x[2]:.3f} / {x[3]:.3f}", x[4], x[5], f"{x[6]:.3f} / {x[7]:.4f} / {x[8]:.4f}")


if __name__ == "__main__":
    main()
