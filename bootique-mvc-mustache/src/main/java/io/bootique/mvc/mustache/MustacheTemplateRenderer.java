/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.mvc.mustache;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.bootique.mvc.Template;
import io.bootique.mvc.cache.ViewCache;
import io.bootique.mvc.renderer.TemplateRenderer;

public class MustacheTemplateRenderer implements TemplateRenderer {

	private MustacheFactory mustacheFactory;
	private ViewCache cache;

	public MustacheTemplateRenderer() {
		this(null);
	}

	public MustacheTemplateRenderer(ViewCache cache) {
		this.mustacheFactory = new DefaultMustacheFactory();
		this.cache = cache;
	}

	@Override
	public void render(Writer out, Template template, Object rootModel) throws IOException {

		Mustache mustache;
		if (cache != null) {
			mustache = cache.contains(template.getName()) ? (Mustache) cache.get(template.getName()) : compile(template);
			cache.add(template.getName(), mustache);
		} else {
			mustache = compile(template);
		}

		mustache.execute(out, rootModel).flush();
	}

	Mustache compile(Template template) {
		// presumably Mustache closes the reader on its own...
		Reader reader = template.reader();
		return mustacheFactory.compile(reader, template.getName());
	}
}
