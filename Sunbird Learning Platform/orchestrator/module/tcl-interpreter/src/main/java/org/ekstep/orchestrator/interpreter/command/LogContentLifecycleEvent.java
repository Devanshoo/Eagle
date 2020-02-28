package org.ekstep.orchestrator.interpreter.command;

import java.util.Map;

import org.ekstep.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class LogContentLifecycleEvent extends BaseSystemCommand implements ICommand, Command {
	
    @Override
    public String getCommandName() {
        return "log_content_lifecycle_event";
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length == 3) {
            try {
                TclObject tclObject1 = argv[1];
                TclObject tclObject2 = argv[2];
                if (null == tclObject1 || null == tclObject2) {
                    throw new TclException(interp, "Null arguments to " + getCommandName());
                } else {
                    String contentId = tclObject1.toString();
                    Object obj2 = ReflectObject.get(interp, tclObject2);
                    Map<String, Object> metadataMap = (Map<String, Object>) obj2;
                    interp.setResult(true);
                }
            } catch (Exception e) {
                throw new TclException(interp, e.getMessage());
            }
        } else {
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_resp_value command");
        }
	}

}
