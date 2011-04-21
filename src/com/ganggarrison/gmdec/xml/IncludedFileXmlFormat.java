package com.ganggarrison.gmdec.xml;

import org.lateralgm.resources.Include;

import com.ganggarrison.easyxml.XmlReader;
import com.ganggarrison.easyxml.XmlWriter;
import com.ganggarrison.gmdec.DeferredReferenceCreatorNotifier;
import com.ganggarrison.gmdec.LgmConst;

public class IncludedFileXmlFormat extends XmlFormat<Include> {
	public enum ExportOption implements LgmConst.Provider {
		NO_AUTO_EXPORT(0),
		TEMP_DIRECTORY(1),
		WORKING_DIRECTORY(2),
		OTHER_DIRECTORY(3);

		public final byte constant;

		public byte getLgmConst() {
			return constant;
		}

		private ExportOption(int constant) {
			this.constant = (byte) constant;
		}
	}

	@Override
	public Include read(XmlReader reader, DeferredReferenceCreatorNotifier notifier) {
		Include include = new Include();
		reader.enterElement("include");
		{
			include.filename = reader.getStringElement("filename");
			include.filepath = reader.getStringElement("filepath");
			include.isOriginal = reader.getBoolElement("original");
			include.size = reader.getIntElement("originalSize");

			if (reader.getBoolElement("hasContent")) {
				include.data = new byte[0];
			} else {
				include.data = null;
			}

			include.export = LgmConst.fromString(reader.getStringElement("exportTo"), ExportOption.class);
			include.exportFolder = reader.getStringElement("otherExportDirectory");
			include.overwriteExisting = reader.getBoolElement("overwriteExisting");
			include.freeMemAfterExport = reader.getBoolElement("freeMemAfterExport");
			include.removeAtGameEnd = reader.getBoolElement("removeAtGameEnd");
		}
		reader.leaveElement();
		return include;
	}

	@Override
	public void write(Include include, XmlWriter writer) {
		writer.startElement("include");
		{
			writer.putElement("filename", include.filename);
			writer.putElement("filepath", include.filepath);
			writer.putElement("original", include.isOriginal);
			writer.putElement("originalSize", include.size);
			writer.putElement("hasContent", include.data != null);

			writer.putElement("exportTo", LgmConst.toString((byte) include.export, ExportOption.class));
			writer.putElement("otherExportDirectory", include.exportFolder);
			writer.putElement("overwriteExisting", include.overwriteExisting);
			writer.putElement("freeMemAfterExport", include.freeMemAfterExport);
			writer.putElement("removeAtGameEnd", include.removeAtGameEnd);
		}
		writer.endElement();
	}
}
