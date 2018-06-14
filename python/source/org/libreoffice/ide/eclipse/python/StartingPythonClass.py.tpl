# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.


import uno, unohelper
from com.sun.star.task import XJobExecutor
from com.sun.star.document import XEventListener


class {0}(unohelper.Base, XJobExecutor, XEventListener):
    
    def trigger(self, args):
        
    # boilerplate code below this point
    def __init__(self, context):
        
    def createUnoService(self, name):
        
    def disposing(self, args):
        pass
    def notifyEvent(self, args):
        pass

g_ImplementationHelper = unohelper.ImplementationHelper()
g_ImplementationHelper.addImplementation(
    {0},
    "org.libreoffice.{0}",
    ("com.sun.star.task.JobExecutor",))
