package com.jtransc.media.limelibgdx.glsl;

import com.jtransc.media.limelibgdx.flash.agal.Agal;
import com.jtransc.media.limelibgdx.flash.agal.GlSlToAgal;
import com.jtransc.media.limelibgdx.glsl.ast.Shader;
import com.jtransc.media.limelibgdx.util.Tokenizer;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class GlSlParserTest {
	private String fragmentShader = (""
		+ "#ifdef GL_ES\n"
		+ "#define LOWP lowp\n"
		+ "precision mediump float;\n"
		+ "#else\n"
		+ "#define LOWP\n"
		+ "#endif\n"
		+ "varying LOWP vec4 v_color;\n"
		+ "varying vec2 v_texCoords;\n"
		+ "uniform sampler2D u_texture;\n"
		+ "void main()\n"
		+ "{\n"
		+ "	gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n"
		+ "}\n"
	);

	String vertexShader = (""
		+ "attribute vec4 a_position;\n" //
		+ "attribute vec4 a_color;\n" //
		+ "attribute vec2 a_texCoord0;\n" //
		+ "uniform mat4 u_projTrans;\n" //
		+ "varying vec4 v_color;\n" //
		+ "varying vec2 v_texCoords;\n" //
		+ "\n" //
		+ "void main()\n" //
		+ "{\n" //
		+ "   v_color = a_color;\n" //
		+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
		+ "   v_texCoords = a_texCoord0;\n" //
		+ "   gl_Position =  u_projTrans * a_position;\n" //
		+ "}\n"
	);

	HashMap<String, String> MACROS_GLES = new HashMap<String, String>() {{
		put("GL_ES", "1");
	}};

	HashMap<String, String> MACROS_EMPTY = new HashMap<String, String>() {{
	}};

	@Test
	public void testPreprocessor() throws Exception {
		Assert.assertEquals(
			String.join("\n",
				"precision mediump float;",
				"varying lowp vec4 v_color;",
				"varying vec2 v_texCoords;",
				"uniform sampler2D u_texture;",
				"void main()",
				"{",
				"	gl_FragColor = v_color * texture2D(u_texture, v_texCoords);",
				"}"
			),
			GlSlParser.preprocess(fragmentShader, MACROS_GLES)
		);

		Assert.assertEquals(
			String.join("\n",
				"varying  vec4 v_color;",
				"varying vec2 v_texCoords;",
				"uniform sampler2D u_texture;",
				"void main()",
				"{",
				"	gl_FragColor = v_color * texture2D(u_texture, v_texCoords);",
				"}"
			),
			GlSlParser.preprocess(fragmentShader, MACROS_EMPTY)
		);
	}

	@Test
	public void tokenize() throws Exception {
		String[] tokens = Tokenizer.tokenizeStr("hello world, test + 12 + 3").toArray(new String[0]);
		Assert.assertEquals(
			"hello:world:,:test:+:12:+:3",
			String.join(":", tokens)
		);
	}

	@Test
	public void tokenize2() throws Exception {
		String[] tokens = Tokenizer.tokenizeStr("10.1 + 10.2").toArray(new String[0]);
		Assert.assertEquals(
			"10.1:+:10.2",
			String.join(":", tokens)
		);
	}

	@Test
	public void parse() throws Exception {
		Shader fragment = GlSlParser.parse(fragmentShader, MACROS_GLES);
		Shader vertex = GlSlParser.parse(vertexShader, MACROS_GLES);
		//GlSlToAgal.convert(fragment);
		//GlSlToAgal.convert(vertex);

		//System.out.println(":: SIR");
		//for (Sir sir : fragment.functions.get("main").body) System.out.println(sir);

		//System.out.println(":: SIR");
		//for (Sir sir : AstToSir.convert(fragment)) System.out.println(sir);
		//System.out.println(":: IR3");
		//for (Ir3 ir3 : AstToIr3.convert(fragment)) System.out.println(ir3);

		Agal.Program program = GlSlToAgal.convert(vertex, fragment);
		//Agal.Program program = GlSlToAgal.convertTest(fragment);

		Assert.assertEquals(
			String.join("\n", new CharSequence[]{
				"tex t0, vc0, v0",
				"mul op, v1, t0",
			}),
			String.join("\n", (CharSequence[]) program.fragment.sourceCodeArray)
		);

		Assert.assertEquals(
			String.join("\n", new CharSequence[]{
				"mov v1, va0",
				"div t0, vc1, vc2",
				"mul v1.a, v1.a, t0",
				"mov v0, va1",
				"mul op, vc3, va2",
			}),
			String.join("\n", (CharSequence[]) program.vertex.sourceCodeArray)
		);
	}
}