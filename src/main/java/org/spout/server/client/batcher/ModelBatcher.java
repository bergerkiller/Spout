package org.spout.server.client.batcher;

import org.lwjgl.opengl.GL11;
import org.spout.api.render.Renderer;
import org.spout.server.client.mesh.BaseMesh;
import org.spout.server.client.renderer.BatchVertexRenderer;

public class ModelBatcher {
	
	Renderer renderer;
	
	public ModelBatcher(){
		renderer = BatchVertexRenderer.constructNewBatch(GL11.GL_TRIANGLES);
		renderer.enableNormals();
		renderer.enableTextures();
	}
	
	public void drawMesh(BaseMesh mesh){
		
	}
	
	
	
	
	
}
