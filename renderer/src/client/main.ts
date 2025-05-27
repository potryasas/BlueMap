import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';
import { PointerLockControls } from 'three/examples/jsm/controls/PointerLockControls';

class MinecraftRenderer {
  private scene: THREE.Scene;
  private camera: THREE.PerspectiveCamera;
  private renderer: THREE.WebGLRenderer;
  private controls: PointerLockControls;
  private textureLoader: THREE.TextureLoader;
  private chunks: Map<string, THREE.Mesh>;
  private moveForward: boolean = false;
  private moveBackward: boolean = false;
  private moveLeft: boolean = false;
  private moveRight: boolean = false;
  private moveUp: boolean = false;
  private moveDown: boolean = false;
  private velocity: THREE.Vector3;
  private direction: THREE.Vector3;

  constructor() {
    // Initialize Three.js scene
    this.scene = new THREE.Scene();
    this.scene.background = new THREE.Color(0x87ceeb); // Sky blue

    // Initialize camera
    this.camera = new THREE.PerspectiveCamera(
      75,
      window.innerWidth / window.innerHeight,
      0.1,
      1000
    );
    this.camera.position.set(0, 16, 0);

    // Initialize renderer
    this.renderer = new THREE.WebGLRenderer({ antialias: true });
    this.renderer.setSize(window.innerWidth, window.innerHeight);
    this.renderer.setPixelRatio(window.devicePixelRatio);
    document.body.appendChild(this.renderer.domElement);

    // Initialize controls
    this.controls = new PointerLockControls(this.camera, document.body);
    this.setupControls();

    // Initialize movement
    this.velocity = new THREE.Vector3();
    this.direction = new THREE.Vector3();

    // Initialize chunks storage
    this.chunks = new Map();
    this.textureLoader = new THREE.TextureLoader();

    // Add lighting
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
    this.scene.add(ambientLight);

    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.6);
    directionalLight.position.set(1, 1, 1);
    this.scene.add(directionalLight);

    // Handle window resize
    window.addEventListener('resize', this.onWindowResize.bind(this));

    // Start render loop
    this.animate();
  }

  private setupControls(): void {
    document.addEventListener('click', () => {
      this.controls.lock();
    });

    document.addEventListener('keydown', (event) => {
      switch (event.code) {
        case 'ArrowUp':
        case 'KeyW':
          this.moveForward = true;
          break;
        case 'ArrowDown':
        case 'KeyS':
          this.moveBackward = true;
          break;
        case 'ArrowLeft':
        case 'KeyA':
          this.moveLeft = true;
          break;
        case 'ArrowRight':
        case 'KeyD':
          this.moveRight = true;
          break;
        case 'Space':
          this.moveUp = true;
          break;
        case 'ShiftLeft':
          this.moveDown = true;
          break;
      }
    });

    document.addEventListener('keyup', (event) => {
      switch (event.code) {
        case 'ArrowUp':
        case 'KeyW':
          this.moveForward = false;
          break;
        case 'ArrowDown':
        case 'KeyS':
          this.moveBackward = false;
          break;
        case 'ArrowLeft':
        case 'KeyA':
          this.moveLeft = false;
          break;
        case 'ArrowRight':
        case 'KeyD':
          this.moveRight = false;
          break;
        case 'Space':
          this.moveUp = false;
          break;
        case 'ShiftLeft':
          this.moveDown = false;
          break;
      }
    });
  }

  private onWindowResize(): void {
    this.camera.aspect = window.innerWidth / window.innerHeight;
    this.camera.updateProjectionMatrix();
    this.renderer.setSize(window.innerWidth, window.innerHeight);
  }

  private async loadChunk(x: number, y: number, z: number): Promise<void> {
    const chunkKey = `${x},${y},${z}`;
    if (this.chunks.has(chunkKey)) return;

    try {
      const response = await fetch(`/api/chunk/${x}/${y}/${z}`);
      const chunkData = await response.json();

      // Create geometry
      const geometry = new THREE.BufferGeometry();
      geometry.setAttribute('position', new THREE.Float32BufferAttribute(chunkData.vertices, 3));
      geometry.setAttribute('normal', new THREE.Float32BufferAttribute(chunkData.normals, 3));
      geometry.setAttribute('uv', new THREE.Float32BufferAttribute(chunkData.uvs, 2));
      geometry.setIndex(chunkData.indices);

      // Load texture atlas
      const atlasResponse = await fetch('/api/textures/atlas');
      const atlasData = await atlasResponse.json();
      const texture = await this.textureLoader.loadAsync(atlasData.atlas);
      texture.magFilter = THREE.NearestFilter;
      texture.minFilter = THREE.NearestFilter;

      // Create material
      const material = new THREE.MeshStandardMaterial({
        map: texture,
        side: THREE.FrontSide,
        alphaTest: 0.1
      });

      // Create mesh
      const mesh = new THREE.Mesh(geometry, material);
      mesh.position.set(x * 16, y * 16, z * 16);
      this.scene.add(mesh);
      this.chunks.set(chunkKey, mesh);
    } catch (error) {
      console.error('Failed to load chunk:', error);
    }
  }

  private updateMovement(): void {
    if (!this.controls.isLocked) return;

    const delta = 0.1;
    this.velocity.x -= this.velocity.x * 10.0 * delta;
    this.velocity.z -= this.velocity.z * 10.0 * delta;
    this.velocity.y -= this.velocity.y * 10.0 * delta;

    this.direction.z = Number(this.moveForward) - Number(this.moveBackward);
    this.direction.x = Number(this.moveRight) - Number(this.moveLeft);
    this.direction.y = Number(this.moveUp) - Number(this.moveDown);
    this.direction.normalize();

    if (this.moveForward || this.moveBackward) {
      this.velocity.z -= this.direction.z * 400.0 * delta;
    }
    if (this.moveLeft || this.moveRight) {
      this.velocity.x -= this.direction.x * 400.0 * delta;
    }
    if (this.moveUp || this.moveDown) {
      this.velocity.y += this.direction.y * 400.0 * delta;
    }

    this.controls.moveRight(-this.velocity.x * delta);
    this.controls.moveForward(-this.velocity.z * delta);
    this.camera.position.y += this.velocity.y * delta;
  }

  private animate(): void {
    requestAnimationFrame(this.animate.bind(this));
    this.updateMovement();

    // Load chunks around player
    const playerChunkX = Math.floor(this.camera.position.x / 16);
    const playerChunkY = Math.floor(this.camera.position.y / 16);
    const playerChunkZ = Math.floor(this.camera.position.z / 16);
    const renderDistance = 2;

    for (let x = -renderDistance; x <= renderDistance; x++) {
      for (let y = -renderDistance; y <= renderDistance; y++) {
        for (let z = -renderDistance; z <= renderDistance; z++) {
          this.loadChunk(
            playerChunkX + x,
            playerChunkY + y,
            playerChunkZ + z
          );
        }
      }
    }

    this.renderer.render(this.scene, this.camera);
  }
}

// Start the application
new MinecraftRenderer(); 