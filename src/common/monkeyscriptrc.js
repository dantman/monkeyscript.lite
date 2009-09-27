// -*- coding: UTF-8 -*-

monkeyscript.hooks['banana'] = __DIR__+'/banana/banana.js';
monkeyscript.hooks['jake'] = __DIR__+'/jake/jake.js';
monkeyscript.hooks['repl'] = __DIR__+'/repl.js';
require.paths.push(__DIR__+'/modules');
