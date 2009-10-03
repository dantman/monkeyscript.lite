// -*- coding: UTF-8 -*-

if ( "xxx".length !== 3 )
	Kernel.die("Engine is not calculating normal characters correctly");
if ( "\u2665".length !== 1 )
	Kernel.die("Engine returns invalid lengths for internally represented multi-byte characters even via \\uXXXX");
if ( "♥".length !== 1 )
	Kernel.die("Engine fails to correctly calculate multibyte UTF-8 characters");

print("JavaScript Engine's String handling is not broken");

