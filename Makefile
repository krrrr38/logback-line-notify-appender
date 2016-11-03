MAVEN := mvn

release: clean
	$(MAVEN) -Prelease release:clean release:prepare release:perform

snapshot: clean
	$(MAVEN) -Prelease deploy

clean:
	$(MAVEN) clean

.PHONY: release snapshot clean
