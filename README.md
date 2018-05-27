# Rubix

This project aims to provide a complete end-to-end architecture for the representation and solution of
Rubik's cube problems. In this document we explain the various problems that were encountered in the course 
of this project and the solutions that were a result of the research and analysis on those problems.

The project can be broken down into some distinct modules:
- Representation of the Rubik's cube
- Solution to scrambled cubes
- Graphical representation of Rubik's cubes

## Problem #1 :
Design a data structure for the representation of a Rubik's cube such that all moves can be performed in O(n) time.

## Solution:

### _Observations:_
- #1. The Rubik's cube is made composed of smaller blocks.
- #2. There are certain invariants that are needed to be considered:
	- The direction system for space in general.
	- The relative position of adjacent faces on any block.
	- The colors inscribed on the block
	
	Here, by the term 'direction system' we refer to the nomenclature of geometrical directions
	namely TOP, BOTTOM, RIGHT, LEFT, FRONT and REAR which is evidently an invariant
	as it is not subject to any perturbations. When considered in conjunction to the
	second point, it essentially means the following: 
	In the event of rotation of cube, the face pointing in a particular direction is 
	replaced by another face or alternatively the said face changes ot direction.
	However the relative position of the faces remain the same. Let us consider
	a horizontal clockwise rotation of a block. The following transformations take place:

	|Face        | New direction|
	|------------|--------------|
	|FRONT       | LEFT         |
	|LEFT        | REAR         |
	|REAR        | RIGHT        |
	|RIGHT       | FRONT        |

- #3. The Rubik's cube can also considered to composed of several substructures. In this
case the Rubik's cube is composed of faces, edges and corners. Hence, every cube has 6 faces, 
12 edges and 8 corners.

### _Structural representation:_
- The Rubik's cube may be represented as a graph of blocks. Each block has two sets of references -- the 
references that map the directions of the block to the corresponding subaxes, and the references that 
map the sub-axes to the adjacent cubes that they point to. Now the mapping from the direction to the
sub-axes should be referred to as the definition, of the directions for that particular block, 
since, they define what the directions refer to in the context of the given block. Here the edges would
be the mapping from the sub-axes to the adjacent block. Note how the notion of the definition fits nicely here --
*the definition of the directions define the structure of the graph*. The direction may ultimately refer
to an adjacent block or a face of the block.
- In the event of rotation of a block, only the definition of the directions need to be changed. The other
mapping can be left intact. This property proves immensely useful in the rotation of an entire face.

### _Implementation:_
- The concept of a block can be reduced to an interface. For any block we would like to query and insert the following:
	- Query the adjacent block in a particular direction
	- Insert a block adjacent to this block in a particular direction
	- Query the color of a face of block in a particular direction
	
- Blocks can be further organized into several substructures. In our case, they shall be *edges*, *faces* and *corners*. Each of them
act as a data structure for the query and insertion of cubes. More specifically they are sub-graph units of the cube graph.

- Each of these substructures provide their internal implementation of the block interface. It is up-to the internal
implementation to handle the changes required in the definition of the directions for the blocks under its control
in the event of cube operations.

- In the case of edge and corners, the definition of all the cubes need to be updated since they are subject to frequent perturbations.
However, for a particular face, a single definition may be applied to the entire face, since all the blocks in a face have a common
outward normal. However, in the event of a cube operation, when a block moves from one face to another, the definition of the
all the cubes need to be synced, IE. both the mappings of reference of the imported blocks need to be synced with the other blocks in the same face.
