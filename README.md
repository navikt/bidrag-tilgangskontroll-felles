# bidrag-tilgangskontroll
Tilgangskontroll for bidrag mikrotjenester


## Deploy

#### Manuell deploy til github packages

bidrag-tilgangskontroll-artifacten kan deployes manuelt til github packages med følgende CURL-kommando (eksempelet viser deploy av versjon 1.2):

<code>
curl -X PUT \
	"https://maven.pkg.github.com/navikt/bidrag-tilgangskontroll/no/nav/bidrag.tilgangskontroll/1.2/bidrag-tilgangskontroll-1.2.jar" \
	-H "Authorization: token {lim inn PAT her}" \
	--upload-file "/C/workspaces/intellij/github/navikt/bidrag-tilgangskontroll/target/bidrag-tilgangskontroll-1.2.jar" -vvv
</code>

