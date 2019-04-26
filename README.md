# Garland Mesh Simplification

## Report

Both Part 1 and Part 2 are working!

I have screenshots of my results labelled and included in the `screenshots` folder. Those screenshots are also displayed in this Markdown file.

## Results

### Part 1

Removing vertices and collapsing edges works well on the sphere.

After removing 1 vertex...

<img src="screenshots/n1.png" width="300"/> <img src="screenshots/n1-wire.png" width="300"/>

After removing 2 vertices...

<img src="screenshots/n2.png" width="300"/> <img src="screenshots/n2-wire.png" width="300"/>

After removing 3 vertices...

<img src="screenshots/n3.png" width="300"/> <img src="screenshots/n3-wire.png" width="300"/>

After collapsing 1 edge...

<img src="screenshots/m1.png" width="300"/> <img src="screenshots/m1-wire.png" width="300"/>

After collapsing 2 edges...

<img src="screenshots/m2.png" width="300"/> <img src="screenshots/m2-wire.png" width="300"/>

After collapsing 3 edges...

<img src="screenshots/m3.png" width="300"/> <img src="screenshots/m3-wire.png" width="300"/>

### Part 2

Running Garland's method of mesh simplification on the sphere (with **stopping ratio of 0.5**) yields good results.

<img src="screenshots/simplify-sph.png" width="300"/> <img src="screenshots/simplify-sph-wire.png" width="300"/>

However, running mesh simplification on the Stanford bunny (with a **stopping ratio of 0.005**) seems to deform the mesh quite a bit.

<img src="screenshots/simplify-bunny.png" width="300"/> <img src="screenshots/simplify-bunny-wire.png" width="300"/>

With a higher stopping ratio--like 0.05--the bunny keeps its shape well.

<img src="screenshots/simplify-bunny2.png" width="300"/> <img src="screenshots/simplify-bunny2-wire.png" width="300"/>

## Usage

Before running the program, edit `build.xml` to specify what .obj file to load and what stopping ratio to use for simplification.

```sh
// Build and execute the program
$ ant
```

## Controls

| Key              | Action                                                    |
|------------------|-----------------------------------------------------------|
| X                | Simplify the mesh                                         |
| N                | Remove a vertex                                           |
| M                | Collapse an edge                                          |
| Z                | Toggle wireframe mesh                                     |
| D                | Enable debug mode (displays MAC grid and cell velocities) |
| U                | Translate current GameObject up                           |
| I                | Translate current GameObject down                         |
| O                | Translate current GameObject forward                      |
| P                | Translate current GameObject backward                     |
| A                | Rotate current GameObject positively about the x-axis     |
| S                | Rotate current GameObject negatively about the x-axis     |
| D                | Rotate current GameObject positively about the y-axis     |
| F                | Rotate current GameObject negatively about the y-axis     |
| G                | Rotate current GameObject positively about the z-axis     |
| H                | Rotate current GameObject negatively about the z-axis     |
| 1                | Take a screenshot                                         |

