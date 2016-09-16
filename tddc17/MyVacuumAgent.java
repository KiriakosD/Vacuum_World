package tddc17;


import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.Random;


/*TO_DO_LIST

*  stage == 0
- First turn North
- Update max_X, max_Y, min_X and min_Y after every FORWARD 		max_y_xy (the position of the max y)
- if (y position > max_y) then go forward (north)
- if position is updated then update x,y for max_y
- Go North until bump
- If bump north, turn RF, LF
- If bump after RF, skip the LF
- If bump after LF, you do a RF, LF
- If !bump after LF, you do a LF again
- stage == -1 tempstage = 0

* TRANSITION (stage = -1)
- try to go to target layer with the above rules
 - if target layer reached then stage = tempstage + 1

*  INSIDE ((if stage== 1) && (layer == something))
-
-
-
-
*
*/

class MyAgentState
{
	public int[][] world = new int[30][30];
	public int[][] memory = new int[30][30]; // Table used for internal memory.
	public int initialized = 0;
	final int UNKNOWN 	= 0;
	final int WALL 		= 1;
	final int CLEAR 	= 2;
	final int DIRT		= 3;
	final int HOME		= 4;
	final int MAXIY		= 5; // Marks the block with the max (or rather min) Y
	final int ACTION_NONE 			= 0;
	final int ACTION_MOVE_FORWARD 	= 1;
	final int ACTION_TURN_RIGHT 	= 2;
	final int ACTION_TURN_LEFT 		= 3;
	final int ACTION_SUCK	 		= 4;
	
	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;
	public int agent_old_action = ACTION_NONE; // Holds the action that happened before the agent_last_action
	
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;

	public int stage = 0;
	public int maxY = 21; // Holds the max (or rather min) Y position; the value 21 is just a random big (unreachable) number to start with.
	public int xOfMaxY = 0; // Holds the X position of the max Y
	
	MyAgentState()
	{
		for (int i=0; i < world.length; i++)
			for (int j=0; j < world[i].length ; j++)
				world[i][j] = UNKNOWN;
		world[1][1] = HOME;
		agent_last_action = ACTION_NONE;
	}
	// Based on the last action and the received percept updates the x & y agent position
	public void updatePosition(DynamicPercept p)
	{
		Boolean bump = (Boolean)p.getAttribute("bump");

		if (agent_last_action==ACTION_MOVE_FORWARD && !bump)
	    {
			switch (agent_direction) {
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
				agent_x_position--;
				break;
			}
	    }
		
	}
	
	public void updateWorld(int x_position, int y_position, int info)
	{
		world[x_position][y_position] = info;
		if (memory[x_position][y_position] != MAXIY) {
			memory[x_position][y_position] = info;
		}
	}
	
	/* A method that calculates the number of explored blocks */
	public int calculateExplored() {
		int explored = 0;
		for (int i=0; i < world.length; i++)
		{
			for (int j=0; j < world[i].length ; j++)
			{
				if (world[j][i]==WALL)
					explored+=1;
				else if (world[j][i]==CLEAR)
					explored+=1;
			}
		}
		return explored;
	}

	public void printWorldDebug()
	{
		for (int i=0; i < world.length; i++)
		{
			for (int j=0; j < world[i].length ; j++)
			{
				if (world[j][i]==UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i]==WALL)
					System.out.print(" # ");
				if (world[j][i]==CLEAR)
					System.out.print(" . ");
				if (world[j][i]==DIRT)
					System.out.print(" D ");
				if (world[j][i]==HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}
}

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();
	
	// Here you can define your variables!
	public int iterationCounter = 10;
	public MyAgentState state = new MyAgentState();
	
	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if(action==0) {
		    state.agent_direction = ((state.agent_direction-1) % 4);
		    if (state.agent_direction<0) 
		    	state.agent_direction +=4;
		    state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action==1) {
			state.agent_direction = ((state.agent_direction+1) % 4);
		    state.agent_last_action = state.ACTION_TURN_RIGHT;
		    return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		} 
		state.agent_last_action=state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}
	
	
	@Override
	public Action execute(Percept percept) {
		
		// DO NOT REMOVE this if condition!!!
    	if (initnialRandomActions>0) {
    		return moveToRandomStartPosition((DynamicPercept) percept);
    	} else if (initnialRandomActions==0) {
    		// process percept for the last step of the initial random actions
    		initnialRandomActions--;
    		state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
    	}
		
    	// This example agent program will update the internal agent state while only moving forward.
    	// START HERE - code below should be modified!

		if (state.agent_last_action==state.ACTION_TURN_LEFT) {
		    state.agent_direction = ((state.agent_direction-1) % 4);
		    if (state.agent_direction<0) 
		    	state.agent_direction +=4;
		}
		else if (state.agent_last_action==state.ACTION_TURN_RIGHT) {
			state.agent_direction = ((state.agent_direction+1) % 4);
			System.out.println("About to turn right, agent_direction=" + state.agent_direction);
		}
    	    	
    	System.out.println("x=" + state.agent_x_position);
    	System.out.println("y=" + state.agent_y_position);
    	System.out.println("dir=" + state.agent_direction);
    	
		
	    iterationCounter--;
	    
	    if (iterationCounter==0)
	    	return NoOpAction.NO_OP;

	    DynamicPercept p = (DynamicPercept) percept;
	    Boolean bump = (Boolean)p.getAttribute("bump");
	    Boolean dirt = (Boolean)p.getAttribute("dirt");
	    Boolean home = (Boolean)p.getAttribute("home");
	    System.out.println("percept: " + p);

		// State update based on the percept value and the last action
		state.updatePosition((DynamicPercept)percept);
		if (bump) {
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position-1,state.WALL);
				break;
			case MyAgentState.EAST:
				state.updateWorld(state.agent_x_position+1,state.agent_y_position,state.WALL);
				break;
			case MyAgentState.SOUTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position+1,state.WALL);
				break;
			case MyAgentState.WEST:
				state.updateWorld(state.agent_x_position-1,state.agent_y_position,state.WALL);
				break;
			}
	    }
	    if (dirt)
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.DIRT);
	    else
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);
	    
	    state.printWorldDebug();
	    
	    
	    // Next action selection based on the percept value

	    if (dirt)
	    {
	    	System.out.println("DIRT -> choosing SUCK action!");
	    	state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
	    } 
	    else
	    {
	    	// Updating the MAXIY position, perhaps it should be moved to another place
			if (state.agent_y_position < state.maxY) {
				if (state.xOfMaxY != 0) {
					state.memory[state.xOfMaxY][state.maxY] = state.CLEAR;
				}
				state.maxY=state.agent_y_position;
				state.xOfMaxY=state.agent_x_position;
				state.memory[state.agent_x_position][state.agent_y_position] = state.MAXIY;
			}

			// Checking if we should go to stage 2
	    	if ((state.memory[state.agent_x_position][state.agent_y_position] == state.MAXIY) && (state.stage == 1)) {
	    		if (state.calculateExplored() > 25) { // 25 is a random minimum number and perhaps should be reconsidered
	    			state.stage = 2;
	    		}
	    	}

	    	// Checking if we should go to stage 1 
			if ((bump) && (state.stage == 0)) {
				state.stage = 1;
	    	}

			if (state.stage == 0) {
				System.out.println("stage = 0!!!");
				if (state.agent_direction == state.NORTH) {
					state.agent_last_action=state.ACTION_MOVE_FORWARD;
					return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
				}
				else {
					state.agent_last_action=state.ACTION_TURN_LEFT;
					return LIUVacuumEnvironment.ACTION_TURN_LEFT;
				}
			}
			else if (state.stage == 1)  {
				System.out.println("stage = 1!!!");
				if (state.agent_last_action == state.ACTION_SUCK) {
					state.agent_last_action=state.ACTION_TURN_LEFT;
					return LIUVacuumEnvironment.ACTION_TURN_LEFT;
				}
				if ((state.agent_last_action == state.ACTION_MOVE_FORWARD) && (state.agent_old_action == state.ACTION_TURN_RIGHT) && (!bump)) {
					state.agent_old_action = state.agent_last_action;
					state.agent_last_action=state.ACTION_TURN_LEFT;
					return LIUVacuumEnvironment.ACTION_TURN_LEFT;
				}
				else if ((state.agent_last_action == state.ACTION_MOVE_FORWARD)) {
					state.agent_old_action = state.agent_last_action;
					if (bump) {
						state.agent_old_action = state.agent_last_action;
						state.agent_last_action=state.ACTION_TURN_RIGHT;
						return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
					}
					else {
						state.agent_old_action = state.agent_last_action;
						state.agent_last_action=state.ACTION_TURN_LEFT;
						return LIUVacuumEnvironment.ACTION_TURN_LEFT;
					}
				}
				else if (state.agent_last_action == state.ACTION_TURN_RIGHT) {
					state.agent_old_action = state.agent_last_action;
					state.agent_last_action=state.ACTION_MOVE_FORWARD;
					return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
				}
				else if (state.agent_last_action == state.ACTION_TURN_LEFT) {
					state.agent_old_action = state.agent_last_action;
					state.agent_last_action=state.ACTION_MOVE_FORWARD;
					return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
				}
			}
			else if (state.stage == 2) {
				// Not sure how to proceed after we reach stage 2
				System.out.println("stage = 2!!!");
				return NoOpAction.NO_OP;
			}
			return NoOpAction.NO_OP; // Must be removed
		}
	}
}

public class MyVacuumAgent extends AbstractAgent {
    public MyVacuumAgent() {
    	super(new MyAgentProgram());
	}
}
