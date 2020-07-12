" I don't yet have any idea what to put here. I should probably use it to make
" the Gradle plugin load lazily; no need to waste users' time with starting a
" JVM process every time.
let gradle#javaprg = 'java'

fun gradle#on_event(id, event, args)
	let l:_A = {'id': a:id, 'event', a:event, 'args': a:args}
	call luaeval('require"gradle.state".onEvent(_A.id, _A.event, _A.args)', l:_A)
endf
