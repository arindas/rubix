package rubix.structure;

enum Direction { 
	TOP, BOTTOM, RIGHT, LEFT, FRONT, REAR;
	
	static final Direction[] DIRECTIONS = {TOP, BOTTOM, RIGHT, LEFT, FRONT, REAR};
	
	static Direction[] getPlane(Direction normal) {
		switch(normal) {
			case TOP:    return new Direction[]{REAR, RIGHT, FRONT, LEFT};
			case BOTTOM: return new Direction[]{REAR, LEFT, FRONT, RIGHT};
			case RIGHT:  return new Direction[]{TOP, REAR, BOTTOM, FRONT};
			case LEFT:   return new Direction[]{TOP, FRONT, BOTTOM, REAR};
			case FRONT:  return new Direction[]{TOP, RIGHT, BOTTOM, LEFT};
			default:     return new Direction[]{TOP, LEFT, BOTTOM, RIGHT}; 
		}
	}
	
	static Direction getReverse(Direction d) {
		int i = 0; for(; i < DIRECTIONS.length; i++) {
			if(DIRECTIONS[i] == d) break;
		} i = i % 2 == 0 ? i + 1: i - 1;
		return DIRECTIONS[i];
	}
}

enum Axis {
	X, Y, Z
}

enum Subaxis {
	X_1, X_2, Y_1, Y_2, Z_1, Z_2 ;
	
	static final Subaxis[] SUBAXES = {X_1, X_2, Y_1, Y_2, Z_1, Z_2};
}

enum Color { 
	RED(0xFF0000), GREEN(0x00FF00), YELLOW(0xFFFF00), BLUE(0x0000FF), ORANGE(0xFFA500), WHITE(0xFFFFFF) ;
	
	int colorCode;
	
	Color(int code) { this.colorCode = code;}
	
	static Color getColor(Subaxis axis) {
		switch(axis){
			case X_1: return Color.GREEN;
            case X_2: return Color.BLUE;
            case Y_1: return Color.YELLOW;
            case Y_2: return Color.WHITE;
            case Z_1: return Color.RED;
            default:  return Color.ORANGE;
		}
	}
}