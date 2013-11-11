/*
 * 
 * Copyright [2013] [claudio degioanni claudio.degioanni@proxima-centauri.it]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.proximacentauri.ienergy.osgi.domain;

import java.util.Date;

import javax.measure.DecimalMeasure;
import javax.measure.Measure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

public class Survey {

	private String name = null;
	private Date timestamp = null;
	private Measure<?, ?> value = null;

	static {
		Unit.ONE.alternate("ppm");
		Unit.ONE.alternate("PPM");
//		Unit.ONE.alternate("Var");
//		Unit.ONE.alternate("VAr");
//		Unit.ONE.alternate("VAR");
//		Unit.ONE.alternate("VA");
		//Unit.ONE.alternate("1");
	}

	public Survey(String name, Date timestamp, String value) {
		this.name = name;
		this.timestamp = timestamp;
		setValue(value);
	}

	public Survey() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Measure<?, ?> getValue() {
		return value;
	}

	public void setValue(Measure<?, ?> value) {
		this.value = value;
	}

	public void setValue(String source) {
		this.value = DecimalMeasure.valueOf(source);
	}

	public <Q extends Quantity> void setValue(java.math.BigDecimal decimal, Unit<Q> unit) {
		this.value = DecimalMeasure.valueOf(decimal, unit);
	}

	public <Q extends Quantity> void setValue(java.math.BigDecimal decimal, String unit) {
		this.value = DecimalMeasure.valueOf(decimal, Unit.valueOf(unit));
	}

	public <Q extends Quantity> void setValue(double decimal, Unit<Q> unit) {
		this.value = DecimalMeasure.valueOf(decimal, unit);
	}

	public <Q extends Quantity> void setValue(double decimal, String unit) {
		this.value = DecimalMeasure.valueOf(decimal, Unit.valueOf(unit));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Survey [");
		if (name != null)
			builder.append("name=").append(name).append(", ");
		if (timestamp != null)
			builder.append("timestamp=").append(timestamp).append(", ");
		if (value != null)
			builder.append("value=").append(value);
		builder.append("]");
		return builder.toString();
	}
}
