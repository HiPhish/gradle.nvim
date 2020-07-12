--- Lua interface to Gradle.
--
-- This module provides the user-facing API of the Gradle integration plugin.
-- The exposed functions are meant to be used by users to build their own
-- Gradle features with them. The Vim script Gradle API is also built upon this
-- module.
local M = {}

local state = require 'gradle.state'


-- work in process, do not use!
function M.runTask(task, args, callbacks)
	local job = vim.g.gradle.job_id
	local timestamp = vim.fn.reltimestr(vim.fn.reltime())

	if callbacks then
		for event, callback in pairs(callbacks) do
			state.registerCallback(timestamp, event, callback)
		end
	end

	vim.fn.rpcrequest(job, 'request', 'run-task', vim.fn.getcwd(), task, args, timestamp)
end


--- Perform a handshake with the remote plugin
--
-- @return
--   The string "OK" if everything went well.
function M.handshake()
	local job = vim.g.gradle.job_id
	return vim.fn.rpcrequest(job, 'request', 'handshake')
end


--- Perform no action
--
-- A no-operation function which does connect to the remote plugin, but does
-- nothing else on the Java side. This function is *not* free of side effects,
-- it goes through the entire Java processing chain as any other of its sibling
-- functions do.
--
-- @return
--   Always returns `vim.NIL`, because it does nothing. Note that this is
--   different from returning a Lua `nil`.
function M.noOp()
	local job = vim.g.gradle.job_id
	return vim.fn.rpcrequest(job, 'request', 'no-op')
end


--- Intentionally throw an exception.
--
-- This function always throws and exception. It is not very useful, but it can
-- be used for testing that exceptions are handled correctly.
function M.throwUp()
	local job = vim.g.gradle.job_id
	vim.fn.rpcrequest(job, 'request', 'throw-up')
end


--- Returns a list of all the tasks of a project.
--
-- @param cwd
--   The working directory of the project
--
-- @return
--   A list (sequence) of tasks. Each task is a table with the following
--   keys: name, description, path and group. Each value of a task is a string.
function M.getTasks(cwd)
	local job = vim.g.gradle.job_id
	return vim.fn.rpcrequest(job, 'request', 'get-tasks', cwd)
end


return M
