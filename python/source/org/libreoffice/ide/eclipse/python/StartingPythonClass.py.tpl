# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

# For Debugging type 'pydevd' and request a code-completion.
# Choose the first suggestion and you will see something like:
#    import sys;sys.path.append(r'/some/path/eclipse/plugins/org.python.pydev.core_6.5.0.201809011628/pysrc')
#    import pydevd;pydevd.settrace()
# Place 'pydevd.settrace()' before the line you want the debugger to stop and
# start debugging your extension (right click on project -> 'Debug As' -> 'LibreOffice extension')
# You need to manually switch to debug view in Eclipse then.

import uno, unohelper
from com.sun.star.task import XJobExecutor
from com.sun.star.document import XEventListener


class {0}(unohelper.Base, XJobExecutor, XEventListener):
    
    def trigger(self, args):
        pass
        
    # boilerplate code below this point
    def __init__(self, context):
        pass
        
    def createUnoService(self, name):
        pass

    def disposing(self, args):
        pass

    def notifyEvent(self, args):
        pass

g_ImplementationHelper = unohelper.ImplementationHelper()
g_ImplementationHelper.addImplementation(
    {0},
    "org.libreoffice.{0}",
    ("com.sun.star.task.JobExecutor",))
