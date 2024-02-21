package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;

import com.badlogic.gdx.Gdx;

public class MyGdxGame extends ApplicationAdapter
{
	private SceneManager sceneManager;
	private SceneAsset sceneAsset;
	private Scene scene;
	private PerspectiveCamera camera;
	private Cubemap diffuseCubemap;
	private Cubemap environmentCubemap;
	private Cubemap specularCubemap;
	private Texture brdfLUT;
	private float time;
	private SceneSkybox skybox;
	private DirectionalLightEx light;
        private FirstPersonCameraController cameraController;

	@Override
	public void create() {

            // create scene
            sceneAsset = new GLTFLoader().load(Gdx.files.internal("donought.gltf"));
            scene = new Scene(sceneAsset.scene);
            sceneManager = new SceneManager();
            sceneManager.addScene(scene);

            // setup camera 
            camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            float d = .02f;
            camera.near = d / 1000f;
            camera.far = 200;
            sceneManager.setCamera(camera);

            cameraController = new FirstPersonCameraController(camera);
            Gdx.input.setInputProcessor(cameraController);


            // setup light
            light = new DirectionalLightEx();
            light.direction.set(1, -3, 1).nor();
            light.color.set(Color.WHITE);
            sceneManager.environment.add(light);

            // setup quick IBL (image based lighting)
            IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
            environmentCubemap = iblBuilder.buildEnvMap(1024);
            diffuseCubemap = iblBuilder.buildIrradianceMap(256);
            specularCubemap = iblBuilder.buildRadianceMap(10);
            iblBuilder.dispose();

            // This texture is provided by the library, no need to have it in your assets.
            brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

            sceneManager.setAmbientLight(1f);
            sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
            sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
            sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

            // setup skybox
            skybox = new SceneSkybox(environmentCubemap);
            sceneManager.setSkyBox(skybox);
	}
	
	@Override
	public void resize(int width, int height) {
            sceneManager.updateViewport(width, height);
	}
	
	@Override
	public void render() {
            float deltaTime = Gdx.graphics.getDeltaTime();
            time += deltaTime;

            cameraController.update();

            // render
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            sceneManager.update(deltaTime);     // Update the Scene 
            sceneManager.render();                    // Render the Scene
	}
	
	@Override
	public void dispose() {
            sceneManager.dispose();
            sceneAsset.dispose();
            environmentCubemap.dispose();
            diffuseCubemap.dispose();
            specularCubemap.dispose();
            brdfLUT.dispose();
            skybox.dispose();
	}
}