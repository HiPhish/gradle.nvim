" This is a bootstrap file, it is the entry point of the Neovim plugin. The
" core of gradle.nvim is written as a standalone Java application, but we need
" a way of hooking up the Java application to the editor. This script will be
" sourced by Neovim; it sets up the RPC connection and defines the commands,
" functions and whatnot that users can use.


" ===[ VARIABLES ]=============================================================
" Initialize the channel, do not overwrite existing value (possible if the
" script has been sourced multiple times)
call extend(g:, {'gradle': {}}, 'keep')
call extend(g:gradle, {'job_id': 0}, 'keep')

" Path to binary
let s:bin = expand('<sfile>:p:h')..'/../build/install/gradle.nvim/bin/gradle.nvim'


" ===[ CALLBACKS ]=============================================================
function! s:on_stderr(chan_id, data, name)
	" echom printf('%s: %s', a:name, string(a:data))
endfunction


" ===[ FUNCTIONS ]=============================================================
" List all the build targets; a more useful function would present a menu of
" targets for the user to choose one from, and then run it.
function! s:list_tasks()
	let l:tasks = luaeval('require"gradle".getTasks(_A)', getcwd())
	for l:task in l:tasks
		echo printf("%s	%s	%s\n", l:task.name, l:task.path, l:task.group)
	endfor
endfunction

function! s:run_task(task)
	call rpcrequest(g:gradle.job_id, 'request', 'run-task', getcwd(), a:task)
endfunction


" ===[ SETUP ]=================================================================
" Entry point. Initialise RPC
function! s:connect()
	let l:job_id = s:initRpc(g:gradle.job_id)

	if l:job_id == 0
		echoerr "Gradle: cannot start RPC process"
	elseif l:job_id == -1
		echoerr "Gradle: RPC process is not executable"
	else
		" Mutate our job Id variable to hold the channel ID
		let g:gradle.job_id = l:job_id
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
command! GradleNoOp :echo rpcrequest(g:gradle.job_id, 'request', 'no-op')
command! GradleNoOp :echo luaeval('require"gradle".noOp()')
command! GradleHandshake :echo luaeval('require"gradle".handshake()')
command! GradleThrow :call luaeval('require"gradle".throwUp()')
command! GradleTasks :call s:list_tasks()
command! -nargs=1 GradleRun :call s:run_task('<args>')
