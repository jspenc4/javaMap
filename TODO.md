# TODO - javaMap

## Current Tasks

### Visualization
- [ ] Re-run generate_3d_surface.py with new d³ potential field
  - Copy newPot3.csv from gridded repo
  - Regenerate all 3D visualizations
  - Compare d³ vs d¹ (barriers should be more prominent)
  - Update tour viewpoints if needed

### Testing & Validation
- [ ] Create test suite with synthetic datasets
  - 3x3, 5x5 grids with known population distributions
  - Verify clustering produces expected hierarchy

- [ ] Add unit tests for clustering algorithm
  - Distance calculations
  - Potential/attraction calculations (1/d⁴)
  - Centroid updates after merge
  - Cache hit/miss behavior

- [ ] Extract golden reference test cases
  - USA regions (should find Northeast, Southeast, Midwest, Southwest, West)
  - World regions (should find South Asia, East Asia, etc.)
  - Visual + numerical validation

- [ ] **Create scaling invariance test (PROOF!)**
  - Run on 50 biggest US cities (from tree)
  - Run on 74,000 census tracts
  - Extract top-level regional structure from both
  - They should MATCH → proves algorithm is resolution-independent
  - This is an executable proof of "no magic numbers"

### Code Quality
- [ ] Refactor to use command-line arguments
  - Input CSV path
  - Output path for results
  - Parameters (cache threshold, etc.)
  - Stop hardcoding paths everywhere

- [ ] Add proper logging
  - Replace System.out.println with actual logging framework
  - Progress reporting
  - Performance metrics (cache hits, merge timing)

### Performance Optimization (HIGH PRIORITY)
**Problem:** M4 Pro with 10 cores + 48GB RAM is SLOWER than old PC laptop

- [ ] Profile the code to find bottleneck
  - Run with -XX:+PrintGCDetails to check garbage collection
  - Check if memory-bound or CPU-bound
  - Monitor thermal throttling during run

- [ ] Increase memory for caching
  - Current: Only cache clusters with >100 members
  - With 48GB: Could cache >10 members (way more aggressive)
  - Could precompute distance matrix for populated cells
  - Trade memory for speed (you have 48GB!)

- [ ] Parallelize the clustering algorithm
  - Potential calculations are embarrassingly parallel
  - Could use all 10 performance cores
  - Need to handle shared data structures carefully

- [ ] Try different JVM settings
  - java -Xmx16g (give it more heap)
  - Try -XX:+UseParallelGC or -XX:+UseG1GC
  - Check if ARM JIT is optimizing properly

### 3D Printing
- [ ] Test export_3d_obj.py with new d³ data
- [ ] Generate regional prints (Western hemisphere already ordered!)
- [ ] Create inverted version (population as ridges instead of valleys)
- [ ] Multi-color network visualization
  - Base surface in one color (white)
  - Spider web lines as raised ridges in contrasting color (black)
  - Gaps in network will be physically visible at barriers

### Interactive Visualization (Future)
- [ ] Build interactive tree explorer
  - Click to expand/collapse regions
  - Show population, centroid, merge order for each node
  - Highlight on map when node selected
  - Color code by merge timing

- [ ] Auto-naming system
  - Reverse geocoding for coordinates
  - Scale-based naming (use worldtree.txt as training data)
  - Handle multi-country clusters gracefully

## Completed
- [x] Update .gitignore to exclude large output files
- [x] Commit project_notes.md and documentation
- [x] Commit world tree.docx (manual hierarchy)
- [x] Push to GitHub (https://github.com/jspenc4/javaMap - public)

## Notes

**Key insight from testing discussion:**
The scaling invariance test is not just validation - it's an **executable proof** that the algorithm captures real geographic structure independent of resolution. If 50 cities and 74K tracts produce the same top-level regions, you've proven (computationally) that the structure is emergent, not an artifact.

**Performance mystery:**
Old PC laptop was faster than M4 Pro - something is very wrong. Likely:
- Using only 1 of 10 cores
- Poor memory utilization
- JVM not optimized for ARM
- Thermal throttling

Need to profile and optimize. This should be blazing fast on M4 Pro.
