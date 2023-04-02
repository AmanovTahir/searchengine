package searchengine.services.parser;

import searchengine.model.SiteModel;

import java.util.concurrent.Callable;

public interface SiteParserService extends Callable<SiteModel> {
}
