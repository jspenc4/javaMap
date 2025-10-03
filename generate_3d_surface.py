#!/usr/bin/env python3
"""
Generate interactive 3D surface visualization from gravitational potential data.

Reads newPot3.csv and creates an interactive HTML visualization using Plotly.
"""

import pandas as pd
import numpy as np
import plotly.graph_objects as go
from pathlib import Path

def load_potential_data(csv_path):
    """Load potential field data from CSV."""
    print(f"Loading data from {csv_path}...")

    # Format: A,population,lat,lon,potential
    df = pd.read_csv(csv_path,
                     names=['type', 'pop', 'lat', 'lon', 'potential'],
                     dtype={'type': str, 'pop': float, 'lat': float, 'lon': float, 'potential': float})

    print(f"Loaded {len(df)} data points")
    print(f"Lat range: {df['lat'].min():.2f} to {df['lat'].max():.2f}")
    print(f"Lon range: {df['lon'].min():.2f} to {df['lon'].max():.2f}")
    print(f"Potential range: {df['potential'].min():.0f} to {df['potential'].max():.0f}")

    return df

def reshape_to_grid(df):
    """Reshape 1D data into 2D grid for surface plotting."""
    # Get unique lat/lon values to determine grid dimensions
    unique_lats = sorted(df['lat'].unique(), reverse=True)  # Top to bottom
    unique_lons = sorted(df['lon'].unique())  # Left to right

    nrows = len(unique_lats)
    ncols = len(unique_lons)

    print(f"Grid dimensions: {nrows} rows × {ncols} cols")

    # Create meshgrid
    lon_grid, lat_grid = np.meshgrid(unique_lons, unique_lats)

    # Reshape potential values to match grid
    # Sort by lat (descending) then lon (ascending)
    df_sorted = df.sort_values(['lat', 'lon'], ascending=[False, True])
    potential_grid = df_sorted['potential'].values.reshape(nrows, ncols)

    return lon_grid, lat_grid, potential_grid

def create_surface_plot(lon_grid, lat_grid, potential_grid, title="World Population Gravitational Potential"):
    """Create interactive 3D surface plot."""
    print("Creating 3D surface plot...")

    # Normalize potential for better visualization (log scale works well for population data)
    z_normalized = np.log10(potential_grid + 1)  # +1 to avoid log(0)

    fig = go.Figure(data=[go.Surface(
        x=lon_grid,
        y=lat_grid,
        z=z_normalized,
        colorscale='Viridis',
        colorbar=dict(
            title=dict(text="log₁₀(Potential)", side="right"),
            tickmode="linear",
        ),
        lighting=dict(
            ambient=0.4,
            diffuse=0.8,
            specular=0.2,
            roughness=0.5,
            fresnel=0.2
        ),
        hovertemplate='<b>Location</b><br>' +
                      'Lon: %{x:.2f}<br>' +
                      'Lat: %{y:.2f}<br>' +
                      'Potential: %{z:.2f}<br>' +
                      '<extra></extra>'
    )])

    fig.update_layout(
        title=dict(
            text=title,
            x=0.5,
            xanchor='center'
        ),
        scene=dict(
            xaxis_title='Longitude',
            yaxis_title='Latitude',
            zaxis_title='log₁₀(Gravitational Potential)',
            camera=dict(
                eye=dict(x=1.5, y=1.5, z=1.3)  # Initial camera angle
            ),
            aspectmode='manual',
            aspectratio=dict(x=2, y=1, z=0.5)  # Adjust vertical exaggeration
        ),
        width=1200,
        height=800,
        hovermode='closest'
    )

    return fig

def create_hemisphere_view(lon_grid, lat_grid, potential_grid, hemisphere='western'):
    """Create view focused on specific hemisphere."""
    if hemisphere == 'western':
        # Western Hemisphere: Americas
        mask = (lon_grid >= -180) & (lon_grid <= -30)
        title = "Western Hemisphere (Americas)"
    elif hemisphere == 'eastern':
        # Eastern Hemisphere: Europe/Africa/Asia
        mask = (lon_grid >= -30) & (lon_grid <= 180)
        title = "Eastern Hemisphere (Europe/Africa/Asia)"
    elif hemisphere == 'asia':
        # Asia focus
        mask = (lon_grid >= 60) & (lon_grid <= 150) & (lat_grid >= 0) & (lat_grid <= 60)
        title = "Asia Pacific Region"
    else:
        mask = np.ones_like(lon_grid, dtype=bool)
        title = "World"

    lon_subset = np.where(mask, lon_grid, np.nan)
    lat_subset = np.where(mask, lat_grid, np.nan)
    potential_subset = np.where(mask, potential_grid, np.nan)

    return create_surface_plot(lon_subset, lat_subset, potential_subset, title)

def create_guided_tour(lon_grid, lat_grid, potential_grid):
    """Create a guided tour with interesting viewpoints."""
    print("Creating guided tour with key viewpoints...")

    # Define interesting camera positions
    viewpoints = [
        {
            'name': 'Golden Triangle Valley',
            'description': 'Fly through the valley between India and China population mountains',
            'camera': dict(eye=dict(x=-0.5, y=1.8, z=0.3)),
            'center': dict(x=98, y=20, z=0),  # Burma/Myanmar area
            'lon_range': (85, 110),
            'lat_range': (15, 30)
        },
        {
            'name': 'Sahara Desert Valley',
            'description': 'Massive population gap between North and Sub-Saharan Africa',
            'camera': dict(eye=dict(x=0.3, y=1.5, z=0.8)),
            'center': dict(x=15, y=20, z=0),
            'lon_range': (-10, 40),
            'lat_range': (0, 40)
        },
        {
            'name': 'Himalayan Barrier',
            'description': 'Population ridge along Himalayas separating India from China',
            'camera': dict(eye=dict(x=1.2, y=1.5, z=0.6)),
            'center': dict(x=85, y=30, z=0),
            'lon_range': (70, 100),
            'lat_range': (25, 40)
        },
        {
            'name': 'Tokyo-Osaka Peak',
            'description': 'Sharp population spike in Japan',
            'camera': dict(eye=dict(x=1.0, y=0.8, z=1.5)),
            'center': dict(x=138, y=36, z=0),
            'lon_range': (130, 145),
            'lat_range': (30, 42)
        },
        {
            'name': 'Northeast US Megalopolis',
            'description': 'Ridge of connected population peaks from DC to Boston',
            'camera': dict(eye=dict(x=-1.2, y=1.0, z=1.2)),
            'center': dict(x=-75, y=40, z=0),
            'lon_range': (-80, -70),
            'lat_range': (37, 43)
        },
        {
            'name': 'Nile River Ridge',
            'description': 'Population corridor cutting through Sahara desert valley',
            'camera': dict(eye=dict(x=0.5, y=1.8, z=0.5)),
            'center': dict(x=31, y=26, z=0),
            'lon_range': (28, 34),
            'lat_range': (22, 32)
        },
        {
            'name': 'Amazon Basin Valley',
            'description': 'Low population between Andean and Brazilian coastal mountains',
            'camera': dict(eye=dict(x=-0.8, y=1.2, z=0.8)),
            'center': dict(x=-62, y=-5, z=0),
            'lon_range': (-75, -50),
            'lat_range': (-12, 2)
        },
        {
            'name': 'Australian Outback',
            'description': 'Flat desert valley with coastal city spikes on edges',
            'camera': dict(eye=dict(x=1.5, y=-1.2, z=0.7)),
            'center': dict(x=135, y=-25, z=0),
            'lon_range': (115, 155),
            'lat_range': (-40, -10)
        },
        {
            'name': 'Java Population Ridge',
            'description': 'Dense continuous population along Indonesian island',
            'camera': dict(eye=dict(x=1.0, y=-0.5, z=1.3)),
            'center': dict(x=110, y=-7, z=0),
            'lon_range': (105, 115),
            'lat_range': (-10, -5)
        },
        {
            'name': 'Trans-Siberian Corridor',
            'description': 'Population ridge connecting European Russia to Pacific',
            'camera': dict(eye=dict(x=0.8, y=2.0, z=0.4)),
            'center': dict(x=90, y=55, z=0),
            'lon_range': (30, 140),
            'lat_range': (50, 65)
        }
    ]

    tour_figs = []
    for vp in viewpoints:
        # Create subset for this viewpoint
        mask = ((lon_grid >= vp['lon_range'][0]) & (lon_grid <= vp['lon_range'][1]) &
                (lat_grid >= vp['lat_range'][0]) & (lat_grid <= vp['lat_range'][1]))

        lon_subset = np.where(mask, lon_grid, np.nan)
        lat_subset = np.where(mask, lat_grid, np.nan)
        potential_subset = np.where(mask, potential_grid, np.nan)

        # Create figure
        z_normalized = np.log10(potential_subset + 1)

        fig = go.Figure(data=[go.Surface(
            x=lon_subset,
            y=lat_subset,
            z=z_normalized,
            colorscale='Viridis',
            colorbar=dict(title=dict(text="log₁₀(Potential)", side="right")),
            lighting=dict(ambient=0.4, diffuse=0.8, specular=0.2, roughness=0.5, fresnel=0.2)
        )])

        fig.update_layout(
            title=dict(
                text=f"<b>{vp['name']}</b><br><sub>{vp['description']}</sub>",
                x=0.5,
                xanchor='center'
            ),
            scene=dict(
                xaxis_title='Longitude',
                yaxis_title='Latitude',
                zaxis_title='log₁₀(Potential)',
                camera=vp['camera'],
                aspectmode='manual',
                aspectratio=dict(x=2, y=1, z=0.7)
            ),
            width=1200,
            height=800,
            hovermode='closest'
        )

        tour_figs.append((vp['name'], fig))

    return tour_figs

def main():
    # Path to the potential data CSV
    csv_path = Path.home() / "Google Drive" / "My Drive" / "gridded" / "res" / "newPot3.csv"

    if not csv_path.exists():
        print(f"ERROR: File not found: {csv_path}")
        print("Please check the path and try again.")
        return

    # Load and process data
    df = load_potential_data(csv_path)
    lon_grid, lat_grid, potential_grid = reshape_to_grid(df)

    # Create output directory
    Path("output").mkdir(exist_ok=True)

    # Create visualizations
    print("\n=== Generating World View ===")
    fig_world = create_surface_plot(lon_grid, lat_grid, potential_grid)
    world_output = "output/world_surface_3d.html"
    fig_world.write_html(world_output)
    print(f"✓ Saved: {world_output}")

    print("\n=== Generating Western Hemisphere View ===")
    fig_west = create_hemisphere_view(lon_grid, lat_grid, potential_grid, 'western')
    west_output = "output/western_hemisphere_3d.html"
    fig_west.write_html(west_output)
    print(f"✓ Saved: {west_output}")

    print("\n=== Generating Eastern Hemisphere View ===")
    fig_east = create_hemisphere_view(lon_grid, lat_grid, potential_grid, 'eastern')
    east_output = "output/eastern_hemisphere_3d.html"
    fig_east.write_html(east_output)
    print(f"✓ Saved: {east_output}")

    print("\n=== Generating Asia Pacific View ===")
    fig_asia = create_hemisphere_view(lon_grid, lat_grid, potential_grid, 'asia')
    asia_output = "output/asia_pacific_3d.html"
    fig_asia.write_html(asia_output)
    print(f"✓ Saved: {asia_output}")

    print("\n=== Generating Guided Tour ===")
    tour_figs = create_guided_tour(lon_grid, lat_grid, potential_grid)

    tour_outputs = []
    for name, fig in tour_figs:
        # Create safe filename
        filename = name.lower().replace(' ', '_').replace('-', '_')
        output_path = f"output/tour_{filename}.html"
        fig.write_html(output_path)
        tour_outputs.append((name, output_path))
        print(f"✓ Saved: {name} -> {output_path}")

    print("\n" + "="*60)
    print("✓ All visualizations generated successfully!")
    print("="*60)
    print("\nGeneral views:")
    print(f"  - {world_output}")
    print(f"  - {west_output}")
    print(f"  - {east_output}")
    print(f"  - {asia_output}")
    print("\nGuided tour viewpoints:")
    for name, path in tour_outputs:
        print(f"  - {name}: {path}")
    print("\nOpen any HTML file in your browser to view the interactive 3D surface.")
    print("You can rotate, zoom, and hover over the surface to explore the data.")
    print("\nTIP: The guided tour files are pre-positioned at interesting viewpoints!")
    print("     Start with 'Golden Triangle Valley' to fly through Burma/Myanmar.")

if __name__ == "__main__":
    main()
