#include <stdio.h>
#include <e_coreid.h>

/* Add in this module code that is common to all cores */

int mc_core_common_go()
{
	int status = 0;
	e_coreid_t coreid;

	coreid = e_get_coreid();

	printf("Hello world from core (0x%x, 0x%x)\n", ((coreid >> 6) & 0x3f), ((coreid >> 0) & 0x3f));
	
	return status;
}

