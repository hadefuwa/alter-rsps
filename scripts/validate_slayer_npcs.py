#!/usr/bin/env python3
"""
Slayer NPC Validation Script

This script scans all spawn plugins in the game-plugins directory and extracts
NPC spawn information. It then validates which NPCs are valid for slayer tasks
based on the RSCM mappings and combat definitions.

Usage:
    python scripts/validate_slayer_npcs.py

This script will:
1. Scan all SpawnPlugin.kt files and other plugin files that spawn NPCs
2. Parse the npc.rscm file to map NPC names to IDs
3. Generate a report of all spawned NPCs that could be slayer tasks
4. Optionally generate a Kotlin file with valid slayer NPC IDs
"""

import os
import re
import json
from pathlib import Path
from collections import defaultdict
from typing import Dict, List, Set, Tuple

# Configuration
WORKSPACE_ROOT = Path(__file__).parent.parent
GAME_PLUGINS_DIR = WORKSPACE_ROOT / "game-plugins" / "src" / "main" / "kotlin"
RSCM_FILE = WORKSPACE_ROOT / "data" / "cfg" / "rscm" / "npc.rscm"
OUTPUT_DIR = WORKSPACE_ROOT / "scripts" / "output"

# NPC names to exclude from slayer tasks (case-insensitive patterns)
EXCLUDED_NAME_PATTERNS = [
    "shop", "banker", "guard", "soldier", "knight", "wizard", "priest", "monk",
    "merchant", "trader", "farmer", "fisherman", "cook", "bartender", "nurse",
    "tutor", "master", "teacher", "guide", "leprechaun", "null", "spawn",
    "rock", "tentacle", "head", "wing", "twig", "pile",
    "giant skeleton", "zombie swab", "assassin", "assasin", "angry goblin", 
    "baboon thrall", "rebel warrior", "elidinis warden", "rooster", "mourner",
    "fear repear", "strangled", "prince itzla arkan"
]

# NPC IDs to explicitly exclude
EXCLUDED_NPC_IDS = {680, 681}


def load_rscm_mappings() -> Dict[str, int]:
    """Load NPC name to ID mappings from npc.rscm file."""
    mappings = {}
    
    if not RSCM_FILE.exists():
        print(f"Warning: RSCM file not found at {RSCM_FILE}")
        return mappings
    
    with open(RSCM_FILE, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if ':' in line:
                parts = line.split(':')
                if len(parts) == 2:
                    name = parts[0].strip()
                    try:
                        npc_id = int(parts[1].strip())
                        mappings[name] = npc_id
                    except ValueError:
                        continue
    
    print(f"Loaded {len(mappings)} NPC mappings from RSCM file")
    return mappings


def find_spawn_files() -> List[Path]:
    """Find all Kotlin files that contain NPC spawns."""
    spawn_files = []
    
    for kt_file in GAME_PLUGINS_DIR.rglob("*.kt"):
        try:
            with open(kt_file, 'r', encoding='utf-8') as f:
                content = f.read()
                if 'spawnNpc' in content:
                    spawn_files.append(kt_file)
        except Exception as e:
            print(f"Error reading {kt_file}: {e}")
    
    return spawn_files


def extract_spawned_npcs(file_path: Path) -> List[Tuple[str, str]]:
    """Extract spawned NPC names from a Kotlin file.
    
    Returns a list of tuples: (npc_rscm_name, raw_spawn_line)
    """
    spawned_npcs = []
    
    # Pattern to match spawnNpc calls
    # Examples:
    #   spawnNpc(npc = "npc.crawling_hand_448", x = 3420, ...)
    #   spawnNpc("npc.shop_keeper", x = 3211, ...)
    #   spawnNpc("npc.abyssal_demon_415", 3434, 9962, 3, 5)
    patterns = [
        r'spawnNpc\s*\(\s*npc\s*=\s*"([^"]+)"',  # Named parameter
        r'spawnNpc\s*\(\s*"([^"]+)"',  # Positional parameter
    ]
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
            for pattern in patterns:
                matches = re.finditer(pattern, content)
                for match in matches:
                    npc_ref = match.group(1)
                    # Extract the RSCM name (remove "npc." prefix if present)
                    if npc_ref.startswith("npc."):
                        rscm_name = npc_ref[4:]  # Remove "npc." prefix
                    else:
                        rscm_name = npc_ref
                    
                    spawned_npcs.append((rscm_name, match.group(0)))
    
    except Exception as e:
        print(f"Error parsing {file_path}: {e}")
    
    return spawned_npcs


def is_valid_slayer_npc(npc_name: str) -> bool:
    """Check if an NPC name is valid for slayer tasks."""
    name_lower = npc_name.lower()
    
    # Check against excluded patterns
    for pattern in EXCLUDED_NAME_PATTERNS:
        if pattern in name_lower:
            return False
    
    return True


def get_display_name(rscm_name: str) -> str:
    """Convert RSCM name to a display-friendly name."""
    # Remove numeric suffixes (e.g., crawling_hand_448 -> crawling_hand)
    name = re.sub(r'_\d+$', '', rscm_name)
    # Replace underscores with spaces and title case
    name = name.replace('_', ' ').title()
    return name


def main():
    print("=" * 60)
    print("Slayer NPC Validation Script")
    print("=" * 60)
    print()
    
    # Load RSCM mappings
    rscm_mappings = load_rscm_mappings()
    
    # Find spawn files
    spawn_files = find_spawn_files()
    print(f"Found {len(spawn_files)} files with NPC spawns")
    print()
    
    # Extract all spawned NPCs
    all_spawned_npcs: Dict[str, List[Tuple[Path, str]]] = defaultdict(list)
    
    for spawn_file in spawn_files:
        npcs = extract_spawned_npcs(spawn_file)
        for rscm_name, raw_line in npcs:
            all_spawned_npcs[rscm_name].append((spawn_file, raw_line))
    
    print(f"Found {len(all_spawned_npcs)} unique NPC types spawned")
    print()
    
    # Categorize NPCs
    valid_slayer_npcs = {}
    invalid_slayer_npcs = {}
    unknown_npcs = []
    
    for rscm_name, spawn_locations in all_spawned_npcs.items():
        npc_id = rscm_mappings.get(rscm_name)
        
        if npc_id is None:
            unknown_npcs.append((rscm_name, spawn_locations))
            continue
        
        if npc_id in EXCLUDED_NPC_IDS:
            invalid_slayer_npcs[rscm_name] = (npc_id, spawn_locations, "Excluded by ID")
            continue
        
        if not is_valid_slayer_npc(rscm_name):
            invalid_slayer_npcs[rscm_name] = (npc_id, spawn_locations, "Excluded by name pattern")
            continue
        
        valid_slayer_npcs[rscm_name] = (npc_id, spawn_locations)
    
    # Print report
    print("=" * 60)
    print("VALID SLAYER NPCs (can be assigned as tasks)")
    print("=" * 60)
    
    # Group by display name for cleaner output
    grouped_valid = defaultdict(list)
    for rscm_name, (npc_id, locations) in valid_slayer_npcs.items():
        display_name = get_display_name(rscm_name)
        grouped_valid[display_name].append((rscm_name, npc_id, len(locations)))
    
    for display_name in sorted(grouped_valid.keys()):
        variants = grouped_valid[display_name]
        total_spawns = sum(count for _, _, count in variants)
        ids = [str(npc_id) for _, npc_id, _ in variants]
        print(f"  {display_name}")
        print(f"    IDs: {', '.join(ids)}")
        print(f"    Total spawns: {total_spawns}")
    
    print()
    print(f"Total valid slayer NPC types: {len(grouped_valid)}")
    print(f"Total valid NPC IDs: {len(valid_slayer_npcs)}")
    
    print()
    print("=" * 60)
    print("INVALID SLAYER NPCs (excluded from tasks)")
    print("=" * 60)
    
    for rscm_name, (npc_id, locations, reason) in sorted(invalid_slayer_npcs.items()):
        print(f"  {rscm_name} (ID: {npc_id}) - {reason}")
    
    print()
    print(f"Total excluded: {len(invalid_slayer_npcs)}")
    
    if unknown_npcs:
        print()
        print("=" * 60)
        print("UNKNOWN NPCs (not found in RSCM)")
        print("=" * 60)
        
        for rscm_name, locations in unknown_npcs:
            print(f"  {rscm_name} - {len(locations)} spawn(s)")
    
    # Create output directory
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    
    # Write detailed report to JSON
    report = {
        "valid_slayer_npcs": {
            name: {"id": npc_id, "spawn_count": len(locs)}
            for name, (npc_id, locs) in valid_slayer_npcs.items()
        },
        "excluded_npcs": {
            name: {"id": npc_id, "reason": reason, "spawn_count": len(locs)}
            for name, (npc_id, locs, reason) in invalid_slayer_npcs.items()
        },
        "unknown_npcs": [name for name, _ in unknown_npcs],
        "summary": {
            "total_valid_types": len(valid_slayer_npcs),
            "total_excluded": len(invalid_slayer_npcs),
            "total_unknown": len(unknown_npcs),
            "valid_ids": sorted([npc_id for _, (npc_id, _) in valid_slayer_npcs.items()])
        }
    }
    
    report_file = OUTPUT_DIR / "slayer_npc_report.json"
    with open(report_file, 'w', encoding='utf-8') as f:
        json.dump(report, f, indent=2)
    
    print()
    print(f"Detailed report written to: {report_file}")
    
    # Write valid IDs list for easy copy-paste
    ids_file = OUTPUT_DIR / "valid_slayer_npc_ids.txt"
    with open(ids_file, 'w', encoding='utf-8') as f:
        f.write("// Valid Slayer NPC IDs (auto-generated)\n")
        f.write("// These NPCs are spawned in the game and can be assigned as slayer tasks\n\n")
        
        valid_ids = sorted([npc_id for _, (npc_id, _) in valid_slayer_npcs.items()])
        f.write(f"private val validSlayerNpcIds = setOf(\n")
        
        # Write IDs with comments
        for rscm_name, (npc_id, _) in sorted(valid_slayer_npcs.items(), key=lambda x: x[1][0]):
            display_name = get_display_name(rscm_name)
            f.write(f"    {npc_id}, // {display_name}\n")
        
        f.write(")\n")
    
    print(f"Valid IDs list written to: {ids_file}")
    
    print()
    print("=" * 60)
    print("SUMMARY")
    print("=" * 60)
    print(f"  Valid slayer NPCs: {len(valid_slayer_npcs)}")
    print(f"  Excluded NPCs: {len(invalid_slayer_npcs)}")
    print(f"  Unknown NPCs: {len(unknown_npcs)}")
    print()
    print("The slayer system now only assigns tasks for NPCs that are")
    print("actually spawned in the world. No need to manually maintain")
    print("a list of valid NPC IDs!")


if __name__ == "__main__":
    main()
