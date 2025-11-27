#!/usr/bin/env python3
"""
NPC Spawn Extraction Script

This script extracts NPC spawn data from NPCList_OSRS.json for a specific area.
It filters NPCs by coordinate range and groups them by height/plane.

Usage:
    python scripts/extract_npc_spawns.py

Before running, modify the configuration section below with your target area coordinates.
"""

import json
import os
import sys

# Fix Windows console encoding for Unicode characters
if sys.platform == 'win32':
    import codecs
    sys.stdout = codecs.getwriter('utf-8')(sys.stdout.buffer, 'strict')
    sys.stderr = codecs.getwriter('utf-8')(sys.stderr.buffer, 'strict')

# ============================================================================
# CONFIGURATION - Modify these values for your target area
# ============================================================================

# Target area name (for output file naming)
AREA_NAME = "jormungands_prison"

# Coordinate range for filtering NPCs
# Jormungand's Prison coordinates:
# - Center: 2460, 10415
# - Adding buffer around the center point
MIN_X = 2440
MAX_X = 2480
MIN_Y = 10395
MAX_Y = 10435

# Input and output file paths
INPUT_JSON = "docs/NPCList_OSRS.json"
OUTPUT_JSON = f"{AREA_NAME}_npcs.json"

# ============================================================================
# SCRIPT LOGIC - No need to modify below unless adding features
# ============================================================================

def load_npc_data(json_path):
    """Load NPC data from JSON file."""
    try:
        with open(json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        print(f"[OK] Loaded {len(data)} NPCs from {json_path}")
        return data
    except FileNotFoundError:
        print(f"[ERROR] File not found: {json_path}")
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(f"[ERROR] Invalid JSON in {json_path}: {e}")
        sys.exit(1)


def filter_npcs_by_coordinates(npcs, min_x, max_x, min_y, max_y):
    """Filter NPCs within the specified coordinate range."""
    filtered = []
    for npc in npcs:
        x = npc.get('x', 0)
        y = npc.get('y', 0)
        
        if min_x <= x <= max_x and min_y <= y <= max_y:
            filtered.append(npc)
    
    return filtered


def group_npcs_by_height(npcs):
    """Group NPCs by their height/plane (p parameter)."""
    by_height = {}
    for npc in npcs:
        height = npc.get('p', 0)
        if height not in by_height:
            by_height[height] = []
        by_height[height].append(npc)
    
    return by_height


def print_summary(npcs, by_height):
    """Print a summary of extracted NPCs."""
    print(f"\n{'='*60}")
    print(f"NPC Extraction Summary for {AREA_NAME.upper().replace('_', ' ')}")
    print(f"{'='*60}")
    print(f"Coordinate Range: X: {MIN_X}-{MAX_X}, Y: {MIN_Y}-{MAX_Y}")
    print(f"Total NPCs found: {len(npcs)}")
    print(f"\nNPCs by Height:")
    
    for height in sorted(by_height.keys()):
        count = len(by_height[height])
        print(f"  Height {height}: {count} NPCs")
    
    print(f"\n{'='*60}\n")


def print_detailed_list(by_height):
    """Print detailed list of NPCs grouped by height."""
    for height in sorted(by_height.keys()):
        print(f"\n=== Height {height} ===")
        npcs = by_height[height]
        
        # Group by NPC name for better readability
        by_name = {}
        for npc in npcs:
            name = npc.get('name', 'Unknown')
            if name not in by_name:
                by_name[name] = []
            by_name[name].append(npc)
        
        # Print grouped by name
        for name in sorted(by_name.keys()):
            npcs_of_name = by_name[name]
            npc_id = npcs_of_name[0].get('id', 0)
            print(f"\n  {name} (ID: {npc_id}) - {len(npcs_of_name)} spawns:")
            for npc in npcs_of_name:
                x = npc.get('x', 0)
                y = npc.get('y', 0)
                print(f"    - ({x}, {y})")


def save_filtered_data(npcs, output_path):
    """Save filtered NPCs to a JSON file."""
    try:
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(npcs, f, indent=2)
        print(f"[OK] Saved {len(npcs)} NPCs to {output_path}")
    except Exception as e:
        print(f"[ERROR] Error saving to {output_path}: {e}")


def generate_spawn_code_snippet(by_height):
    """Generate a Kotlin code snippet for the spawn plugin."""
    print(f"\n{'='*60}")
    print("Kotlin Spawn Code Snippet (for reference)")
    print(f"{'='*60}\n")
    
    for height in sorted(by_height.keys()):
        print(f"// Height {height}")
        for npc in by_height[height]:
            name = npc.get('name', 'Unknown')
            npc_id = npc.get('id', 0)
            x = npc.get('x', 0)
            y = npc.get('y', 0)
            
            # Generate RSCM name (you'll need to verify this in npc.rscm)
            rscm_name = f"npc.npc_{npc_id}"  # Placeholder - verify actual name
            print(f'spawnNpc(npc = "{rscm_name}", x = {x}, z = {y}, height = {height}, walkRadius = 5, direction = Direction.SOUTH)')
        print()


def main():
    """Main execution function."""
    print("NPC Spawn Extraction Script")
    print("=" * 60)
    
    # Check if input file exists
    if not os.path.exists(INPUT_JSON):
        print(f"[ERROR] Input file not found: {INPUT_JSON}")
        print("  Make sure you're running this script from the project root directory.")
        sys.exit(1)
    
    # Load NPC data
    all_npcs = load_npc_data(INPUT_JSON)
    
    # Filter by coordinates
    print(f"\nFiltering NPCs in range: X: {MIN_X}-{MAX_X}, Y: {MIN_Y}-{MAX_Y}...")
    filtered_npcs = filter_npcs_by_coordinates(all_npcs, MIN_X, MAX_X, MIN_Y, MAX_Y)
    
    if not filtered_npcs:
        print("[ERROR] No NPCs found in the specified coordinate range.")
        print("  Try adjusting MIN_X, MAX_X, MIN_Y, MAX_Y in the script.")
        sys.exit(1)
    
    # Group by height
    by_height = group_npcs_by_height(filtered_npcs)
    
    # Print summary
    print_summary(filtered_npcs, by_height)
    
    # Print detailed list
    print_detailed_list(by_height)
    
    # Save filtered data
    save_filtered_data(filtered_npcs, OUTPUT_JSON)
    
    # Generate code snippet (optional)
    print(f"\n{'='*60}")
    print("Next Steps:")
    print("1. Review the filtered NPCs in the output JSON file")
    print("2. Map NPC IDs to RSCM names using data/cfg/rscm/npc.rscm")
    print("3. Create or update the spawn plugin with the extracted data")
    print("4. Verify all NPCs are correctly mapped and spawned")
    print(f"{'='*60}\n")


if __name__ == "__main__":
    main()

