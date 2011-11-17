/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.gmdec;

import org.lateralgm.file.GmFile;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Resource;
import org.lateralgm.util.PropertyMap;

public class DeferredPropertyReferenceCreator<EnumType extends Enum<EnumType>> implements DeferredReferenceCreator {
	private PropertyMap<EnumType> propertyMap;
	private EnumType property;
	private Class refKind;
	private String refStr;

	public <T extends InstantiableResource<T, ?>> DeferredPropertyReferenceCreator(PropertyMap<EnumType> propertyMap,
			EnumType property, Class<T> refKind,
			String refStr) {
		this.propertyMap = propertyMap;
		this.property = property;
		this.refKind = refKind;
		this.refStr = refStr;
	}

	@Override
	public void createReferences(GmFile gmf) {
		if (refStr != null && !refStr.isEmpty()) {
			Resource<?, ?> res = gmf.resMap.getList(refKind).get(refStr);
			if (res != null) {
				propertyMap.put(property, res.reference);
			} else {
				System.err.println("Warning: Reference to unknown " + property + " " + refStr);
			}
		}
	}
}
