# Hierarchical Population Clustering

Gravitational potential-based clustering algorithm that discovers natural regional structure in population data at multiple scales—from neighborhoods to continents.

## Overview

This project implements a hierarchical clustering algorithm based on gravitational potential between population centers. Starting from individual census tracts or grid cells, it iteratively merges regions with the highest mutual attraction, building a complete hierarchy that reveals natural population structure without prior geographic knowledge.

**Key insight:** The same algorithm discovers both that Central Park divides Manhattan's Upper East and West Sides, and that the Sahara Desert divides North and Sub-Saharan Africa.

## Visualizations

### Spider Web Network Maps

Lines connecting merged population centers, colored by merge order.

![US Census Tracts](res/74k_us.pdf)
*74,000 US census tracts merging hierarchically*

![World Population](res/worldPrintMap.pdf)
*Global population at 15-arcsecond resolution*

### 3D Gravitational Potential Surface

Population distribution rendered as elevation, where height represents cumulative gravitational pull.

![3D Surface](res/3dsurface.png)
*Western Hemisphere gravitational potential*

## Key Findings

The algorithm naturally discovers:

- **Continental regions** (South Asia 1.8B, East Asia 1.5B, Western Hemisphere 927M)
- **Natural barriers** (deserts, oceans, mountain ranges) as low-density boundaries
- **Cultural corridors** (Grand Trunk Road in South Asia emerges as coherent 1B+ person region)
- **Metropolitan structure** (urban cores, suburbs, satellite cities)
- **Micro-geography** (Central Park divides Manhattan neighborhoods)

**Cross-validation:** Census tracts, block groups, and world grid data all converge on the same regional boundaries, confirming the algorithm captures real geographic structure.

## Algorithm

### Gravitational Potential Model

```
potential(i, j) = (population_i × population_j) / distance(i, j)^4
```

The inverse fourth power heavily weights nearby populations, mimicking urban agglomeration while allowing long-range effects.

### Process

1. Start with individual census tracts/grid cells as separate regions
2. Calculate pairwise gravitational potential between all regions
3. Merge the pair with highest potential
4. Update centroid (population-weighted) and recalculate potentials
5. Repeat until all regions merged into one

**Output:** Complete merge tree showing hierarchical structure at all scales.

### Distance Calculation

Approximate spherical distance optimized for speed:

```java
xMiles = deltaLon × 69.0 × cos(averageLatitude)
yMiles = deltaLat × 69.172
distance² = xMiles² + yMiles²
```

Cosine lookup table for latitudes 0-89° avoids repeated trig calculations.

### Performance

- **Complexity:** O(n²) with aggressive caching
- **Cache strategy:** Store potentials for large merged regions (>100 members)
- **Memory management:** Release merged tract contents to handle large datasets

## Data Sources

- **US Census Bureau:** Census tracts (~74k) and block groups (~220k)
- **GPW v4:** Gridded Population of the World (CIESIN)
  - 15-arcsecond resolution (~463m at equator)
  - 2015 population adjusted to UN estimates

## Project Structure

```
src/com/jimspencer/
├── Tracts.java        # Main clustering algorithm with caching
├── Tract.java         # Individual tract or merged region
├── SpiderMap.java     # Alternative edge-based approach
├── MapEdge.java       # Adjacency between tracts
└── Region.java        # Region merging by boundary distance

res/
├── censusTracts.csv        # Input: US census tract data
├── treeOutput.csv          # Output: sequential merge records
├── 74k_us.pdf              # US spider web visualization
├── worldPrintMap.pdf       # World spider web (colored)
├── 3dsurface.png           # 3D potential surface
└── usa6region.jpg          # Physical map with 6 hand-drawn regions

docs/
├── INTERNAL-NOTES.md       # Project organization reference
└── world tree.docx         # Annotated world hierarchy (to 3M pop)
```

## Usage

### Running the Clustering Algorithm

```bash
# Compile
javac -d out src/com/jimspencer/*.java

# Run (edit Tracts.java to select input file)
java -cp out com.jimspencer.Tracts
```

**Input:** CSV with `LONGITUDE,LATITUDE,POPULATION`
**Output:** `res/treeOutput.csv` with merge sequence

### Generating Visualizations

The tree output can be rendered as:
- GeoJSON MultiLineString for web mapping
- PDF using vector graphics tools
- Interactive visualizations (D3.js, Observable, etc.)

See `docs/INTERNAL-NOTES.md` for details on visualization workflow.

## World Population Hierarchy

Top-level structure discovered by the algorithm:

1. **South Asia** (1.8B) — Grand Trunk Road corridor, India/Pakistan/Bangladesh
2. **East Asia** (1.5B) — China, Japan, Korea
3. **Western Hemisphere** (927M) — North/Central/South America
4. **Europe & Middle East** (909M) — Benelux to Tehran, Mediterranean
5. **East Africa** (464M) — Great Lakes, Ethiopia, Mozambique corridor
6. **West Africa** (409M) — Nigerian coast, Sahel
7. **Independent Areas** — Indonesia (245M), SE Asia (109M), Philippines (103M)
8. **Isolated** — Australia (21M), Pacific Northwest (14M), New Zealand (5M)

Complete hierarchy annotated down to 3M population in `docs/world tree.docx`. The algorithm continues to individual tracts but manual annotation became tedious.

## Notable Discoveries

- **Grand Trunk Road** emerges as natural region despite spanning Pakistan/India/Bangladesh borders
- **Florida** separates from Deep South in US clustering
- **Middle East/Indian subcontinent** gap aligns with desert barrier
- **Boundary regions** (Denver, Salt Lake City) are genuinely ambiguous—they could plausibly belong to multiple regions

## Technical Notes

### Known Artifacts

**Equatorial gridding:** Early merges in gridded data show slight horizontal bias at the equator (E-W neighbors equally likely). Not visible at normal viewing distance and doesn't affect regional structure.

**Line crossings:** Spider web lines occasionally cross. This is correct—the algorithm uses potential (not pure geography) so attachment points may seem counterintuitive but reflect population attraction.

### Validation

Cross-validation across multiple datasets confirms robustness:
- US census tracts (74k)
- US block groups (220k)
- World 15-arcsecond grid (millions of cells)

All converge on the same major regional boundaries.

## Future Development

- [ ] Interactive web visualization with zoom/drill-down
- [ ] "Crystal growth" animation of merge sequence
- [ ] Temporal analysis with historical population data
- [ ] Alternative metrics (travel time, economic gravity)
- [ ] Code refactoring and test coverage

## Contributing

This is currently a personal research project. If you're interested in collaborating, extending the work, or using it for academic research, please reach out.

## License

[To be determined]

## Citation

If you use this work, please cite:

```
Spencer, J. (2015-2021). Hierarchical Population Clustering via Gravitational Potential.
[GitHub repository or paper citation once available]
```

## Acknowledgments

Thanks to:
- The printer guy who thought it was cool
- My spouse for tolerating this obsession

---

*"It made me pretty happy, might bring joy to others."*
