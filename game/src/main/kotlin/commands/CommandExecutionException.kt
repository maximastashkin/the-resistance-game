package commands

import java.lang.Exception

class CommandExecutionException(override val message: String): Exception(message)