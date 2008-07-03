package aspects;

import aspectPath.JarInpath;
import aspectPath.VariableInpath;
import aspectPath.ProjectInpath;
import aspectPath.ContainerInpath;

public aspect AdviseClassOnInpath {
	
	before () : execution(public void *..fromVariable()) {
		System.out.println("advised variable!");
	}
	before () : execution(public void *..fromJar()) {
		System.out.println("advised jar!");
	}
	before () : execution(public void *..fromProject()) {
		System.out.println("advised project!");
	}
	before () : execution(public void *..fromContainer()) {
		System.out.println("advised container!");
	}
	
	public static void main(String[] args) {
		new ContainerInpath().fromContainer();
		new ProjectInpath().fromProject();
		new VariableInpath().fromVariable();
		new JarInpath().fromJar();
	}
}
