package figures.support;

import figures.Canvas;
import figures.FigureElement;

/**
 * @author colyer, 19-Feb-2003
 *
 */
public aspect HistoryManagement {

	pointcut canvasHistoryUpdate() :
		call(void Canvas.updateHistory(..));
		
	pointcut getters() : get(* FigureElement+.*);	
		
	declare error : canvasHistoryUpdate() &&
		!within(HistoryManagement) :
		"Only HistoryManager should update history";
		
	pointcut figureElementUpdate() :
		execution(* FigureElement+.set*(..));
		
	after() returning : figureElementUpdate() {
		Canvas.updateHistory();	 
	}

}
