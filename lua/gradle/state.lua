--- Internal state management for Gradle.
local M = {}

local callbacks = {}

--- Register a new callback function.
function M.registerCallback(id, event, callback)
	callbacks[id] = callbacks[id] or {}
	callbacks[id][event] = callback
end

--- Call a stashed event callback for the given ID and event
--
-- Looks up a callback function for a given ID and event, and calls it with the
-- given arguments. If not callback can be found nothing will be done.
--
-- @param id
--   ID of the callback
-- @param event
--   Name of the event (string)
--
-- @return
--   Whatever the callback functions applied to the arguments returns.
function M.onEvent(id, event, args)
	local f = (callbacks[id] or {})[event]
	return f and f(args)
end

