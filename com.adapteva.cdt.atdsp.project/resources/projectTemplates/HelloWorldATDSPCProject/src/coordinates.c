/* set coordinates, used by linker to find dedicated memory "slice" in the external DRAM*/ 

extern int _CORE_ROW_;
asm(".global __CORE_ROW_;");

extern int _CORE_COL_;
asm(".global __CORE_COL_;");


