--- Lua interface to Gradle.
--
-- This module provides the user-facing API of the Gradle integration plugin.
-- The exposed functions are meant to be used by users to build their own
-- Gradle features with them. The Vim script Gradle API is also built upon this
-- module.
local M = {}

local callbacks = {}

-- work in process, do not use!
function M.runTask(task, args, callback)
	local job = vim.g.gradle.job_id
	local timestamp = vim.fn.reltimestr(vim.fn.reltime())
	callbacks.timestamp = callback
	vim.fn.rpcnotify(job, 'notify', 'run-task', vim.fn.getcwd(), task, args, timestamp)
end

--- Perform a handshake with the remote plugin
--
-- @return
--   The string "OK" if everything went well.
function M.handshake()
	local job = vim.g.gradle.job_id
	return vim.fn.rpcrequest(job, 'request', 'handshake')
end

--- Returns a list of all the tasks of a project.
--
-- @param cwd The working directory of the project
-- @return
--   A list (sequence) of tasks. Each task is a table with the following
--   keys: name, description, path and group. Each value of a task is a string.
function M.getTasks(cwd)
	local job = vim.g.gradle.job_id
	return vim.fn.rpcrequest(job, 'request', 'get-tasks', cwd)
end

return M
