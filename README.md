TODO (12/6/20):
- implement pirate pathfinding (in progress)
    - on pirate pathfinding, handle the case where there is no valid path
    - add pirate ai to sail toward next waypoint 
    - (optional)simplify path using Ramer–Douglas–Peucker algorithm
    - track next waypoint in pirate class and check if it gets close enough in pirate tick()
    - keep pirates from colliding somehow
- fix weird collision bug
- replace all instances of MainGame static variables passed as parameters with MainGame static getters 
- extend Menu/MenuManager to implement hud/inventory menus
- implement trading 
    - add datastructures to Village to hold current trade, inventory, etc.
    - implement execution of trades through DockedMenu
- improve map hitboxes 
    - use different rectangle/polygon based on neighbor bitmask instead of always 16x16 square
- add falling off world death
- work on starting area idea
- work on upgrade mechanic
- work on sea monster mechanic
