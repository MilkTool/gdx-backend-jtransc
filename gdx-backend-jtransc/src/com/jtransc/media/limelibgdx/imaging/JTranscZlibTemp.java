package com.jtransc.media.limelibgdx.imaging;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.util.zip.Inflater;

public class JTranscZlibTemp {
	static private boolean hasNativeInflate() {
		return JTranscSystem.usingJTransc() && JTranscSystem.isSys();
	}

	@HaxeMethodBody(target = "sys", value = "" +
		"var u = new haxe.zip.Uncompress(-15);\n" +
		"var src = p0.getBytes();\n" +
		"var dst = haxe.io.Bytes.alloc(p1);\n" +
		"u.execute(src, 0, dst, 0);\n" +
		"u.close();\n" +
		"return HaxeByteArray.fromBytes(dst);\n"
	)
	@HaxeMethodBody("return null;")
	native static private byte[] nativeInflate(byte[] data, int outputSize);

	@HaxeMethodBody(target = "flash", value = "return JA_B.fromBytes(haxe.zip.Uncompress.run(p0.getBytes(), p1));")
	@HaxeMethodBody(target = "sys", value = "return JA_B.fromBytes(haxe.zip.Uncompress.run(p0.getBytes(), p1));")
	static public byte[] uncompress(byte[] data, int outputSize) {
		try {
			if (hasNativeInflate()) {
				return nativeInflate(data, outputSize);
			} else {
				Inflater inflater = new Inflater(false);
				inflater.setInput(data);
				byte[] out = new byte[outputSize];
				int result = inflater.inflate(out);
				return out;
				//return JTranscIoTools.copy(new java.util.zip.InflaterInputStream(new ByteArrayInputStream(data), false), new ByteArrayOutputStream(outputSize)).toByteArray();
				//return JTranscIoTools.copy(new InflaterInputStream(new ByteArrayInputStream(data), false), new ByteArrayOutputStream(outputSize)).toByteArray();
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
