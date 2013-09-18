
TARGET =	$(baseName)



all:	$(TARGET)

-include cores.mk

clean:
	rm -fr Cores
	
$(TARGET):	$(ALL_SREC)
	cat  $(ALL_SREC) > ./Cores/$(TARGET).srec
	