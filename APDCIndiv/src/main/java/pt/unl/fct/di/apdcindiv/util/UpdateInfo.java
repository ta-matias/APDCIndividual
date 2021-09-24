package pt.unl.fct.di.apdcindiv.util;

public class UpdateInfo {
	
	public long capital;
	public long PnL;
	
	public UpdateInfo() { }
	
	public UpdateInfo(long capital, long PnL) {
		this.capital = capital;
		this.PnL = PnL;
	}
	
	public boolean validate() {
		if(capital < 0) return false;
		return true;
	}

}
