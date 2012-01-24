package org.dynasoar.service;

public class DynasoarService {
	private String shortName = null;
	private String name = null;
        private String value = null;

        public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
        public void setdeployed(String value) {
                this.value = value;
        }
        public String getdeployed() {
                return value;
        }
}
