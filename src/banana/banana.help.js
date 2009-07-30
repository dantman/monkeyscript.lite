
function help(action) {
	switch(action) {
	case 'collect':
		print(
' banana collect path/to/registry.json path/to/banana.json ...\n' +
'    Given the location of a registry.json file, and a number of banana.json\n' +
'    files, banana collect reads the banana.json files and builds a new registry\n' +
'    saving it to the specified registry file for use by banana().');
		break;
	}
	return 0;
}

