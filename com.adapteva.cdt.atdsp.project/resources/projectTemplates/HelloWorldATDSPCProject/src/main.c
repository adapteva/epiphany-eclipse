/* Add in this module code that is specific to this core */

int mc_core_common_go();

int main(void)
{
	int status;

	/* jump to multicore common code */
	status = mc_core_common_go();

	return status;
}

