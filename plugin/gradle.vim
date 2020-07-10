" This is a bootstrap file, it is the entry point of the Neovim plugin. The
" core of gradle.nvim is written as a standalone Java application, but we need
" a way of hooking up the Java application to the editor. This script will be
" sourced by Neovim; it sets up the RPC connection and defines the commands,
" functions and whatnot that users can use.


" ===[ VARIABLES ]=============================================================
" Initialize the channel, do not overwrite existing value (possible if the
" script has been sourced multiple times)
call extend(s:, {'job_id': 0}, 'keep')

" Path to binary
let s:bin = expand('<sfile>:p:h')..'/../build/install/gradle.nvim/bin/gradle.nvim'


" ===[ CALLBACKS ]=============================================================
function! s:on_stderr(chan_id, data, name)
	" echom printf('%s: %s', a:name, string(a:data))
endfunction


" ===[ FUNCTIONS ]=============================================================
" List all the build targets; a more useful function would present a menu of
" targets for the user to choose one from, and then run it.
function! s:list_tasks(tasks)
	for [l:name, l:description, l:path, l:group] in a:tasks
		echo printf("%s	%s	%s\n", l:name, l:path, l:group)
	endfor
endfunction

function! s:run_task(task)
	call rpcrequest(s:job_id, 'request', 'run-task', getcwd(), a:task)
endfunction


" ===[ SETUP ]=================================================================
" Entry point. Initialise RPC
function! s:connect()
	let l:job_id = s:initRpc(s:job_id)

	if l:job_id == 0
		echoerr "Gradle: cannot start RPC process"
	elseif l:job_id == -1
		echoerr "Gradle: RPC process is not executable"
	else
		" Mutate our job Id variable to hold the channel ID
		let s:job_id = l:job_id
	endif
endfunction

" Initialize RPC
function! s:initRpc(job_id)
	if a:job_id == 0
		let l:opts = {'rpc': v:true, 'on_stderr': funcref('s:on_stderr')}
		let jobid = jobstart(['sh', s:bin], l:opts)
		return jobid
	else
		return a:job_id
	endif
endfunction

call s:connect()

" Some commands for playing around with
command! GradleNoOp :echo rpcrequest(s:job_id, 'request', 'no-op')
command! GradleHandshake :echo rpcrequest(s:job_id, 'request', 'handshake')
command! GradleThrow :echo rpcrequest(s:job_id, 'request', 'throw-up')
command! GradleTasks :call s:list_tasks(rpcrequest(s:job_id, 'request', 'get-tasks', getcwd()))
command! -nargs=1 GradleRun :call s:run_task('<args>')
