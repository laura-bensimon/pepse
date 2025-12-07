# PEPSE – Procedural Environment & Physics Simulation Engine
University Java Project (HUJI – OOP Course)

This project implements a 2D procedural open-world simulation featuring:
- Day/night cycle with sun and halo animation
- Trees, leaves, fruits and natural growth behavior
- Player avatar physics: jumping and energy management
- Cloud system, rain particles and sky transitions
- Infinite terrain generation using noise
- Event-driven design and object-oriented architecture
- Smooth transitions using scheduled tasks and interpolators

The project is built using the DanoGameLab framework and follows clean OOP principles.

------------------------------------------------------------

## Project Structure

src/
  pepse/
    PepseGameManager.java
    world/
      Avatar.java
      Block.java
      Cloud.java
      CloudGroup.java
      EnergyDisplay.java
      JumpObserver.java
      RainDrop.java
      Sky.java
      Terrain.java
      daynight/
        Sun.java
        SunHalo.java
        Night.java
      trees/
        Tree.java
        Fruit.java
        Flora.java
    util/
      ColorSupplier.java
      NoiseGenerator.java

assets/
  Sprite images for avatar animations and more

------------------------------------------------------------

## Day & Night Cycle

- The Sun moves in a smooth circular motion.
- A halo is rendered around the sun using blend effects.
- The Night class darkens the screen through an opacity transition.
- The cycle uses scheduled behaviors to update continuously.

------------------------------------------------------------

## Trees, Leaves and Fruits – Design Explanation

The trees package is designed around natural growth and modularity.

Tree:
Creates the full structure of a tree including trunk, leaves and fruits.

Fruit:
Implements growth, falling mechanics, regeneration and collision behavior. Fruits detach when impacted or due to gravity.

Flora:
Controls the placement of trees along the terrain. It uses noise-based spacing to achieve natural distribution.

Design rationale:
Separating generation (Flora), structure (Tree) and behavior (Fruit) ensures flexibility, clarity and clean OOP architecture.

------------------------------------------------------------

## Avatar and Physics

- Jumping behavior uses an observer mechanism.
- Movement consumes energy that regenerates over time.
- Animations change depending on movement and state.
- Basic collision and gravity logic included.

------------------------------------------------------------

## Infinite World Generation

Terrain height is generated using noise functions.
New chunks of terrain are dynamically created as the player moves.
This produces the effect of an infinite scrolling world.

------------------------------------------------------------

## How to Run

1. Install the DanoGameLab library.
2. Open the project in IntelliJ or any Java IDE.
3. Run the class: PepseGameManager.
4. Controls:
   - Left / Right arrows: move
   - Space: jump

------------------------------------------------------------

## Author

Laura Bensimon
Computer Science and Data Science student
Hebrew University of Jerusalem
