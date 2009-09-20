// -*- coding: UTF-8 -*-

//exports.stdin = /* @todo */;
//exports.stdout = /* @todo */;
//exports.stderr = /* @todo */;
//exports.env = /* @todo */;
exports.args = Array.slice(monkeyscript.arguments);
exports.print = function() { return exports.stdout.print.apply(exports.stdout, arguments); };
exports.platform = monkeyscript.platform;
exports.global = global;

