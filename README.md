# bidrag-tilgangskontroll-felles
![](https://github.com/navikt/bidrag-tilgangskontroll-felles/workflows/maven%20deploy/badge.svg)
![](https://github.com/navikt/bidrag-tilgangskontroll-felles/workflows/verify%20pull%20request/badge.svg)

Tilgangskontroll for bidrag mikrotjenester

Se [endringer.logg](endringer.logg) for oversikt over versjoner.

## Deploy

#### Manuell deploy til github packages

bidrag-tilgangskontroll-felles-artifacten kan deployes manuelt til github packages med følgende CURL-kommando (eksempelet viser deploy av versjon 1.3):

JAR
<code>
curl -X PUT \
"https://maven.pkg.github.com/navikt/bidrag-tilgangskontroll-felles/no/nav/bidrag/bidrag-tilgangskontroll-felles/1.2/bidrag-tilgangskontroll-1.3.jar" \
-H "Authorization: token {lim inn PAT her}" \
--upload-file "{full sti til lokalt maven repo}/bidrag-tilgangskontroll-felles-1.3.jar" -vvv
</code>

POM
<code>
	curl -X PUT \
	"https://maven.pkg.github.com/navikt/bidrag-tilgangskontroll-felles/no/nav/bidrag/bidrag-tilgangskontroll-felles/1.3/bidrag-tilgangskontroll-1.3.pom" \
	-H "Authorization: token {lim inn PAT her}" \
	--upload-file "{full sti til lokalt maven repo}/bidrag-tilgangskontroll-felles-1.3.pom" -vvv
</code>
