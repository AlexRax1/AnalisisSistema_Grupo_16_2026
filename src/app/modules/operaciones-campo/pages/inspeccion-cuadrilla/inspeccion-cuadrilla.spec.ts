import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InspeccionCuadrilla } from './inspeccion-cuadrilla';

describe('InspeccionCuadrilla', () => {
  let component: InspeccionCuadrilla;
  let fixture: ComponentFixture<InspeccionCuadrilla>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InspeccionCuadrilla],
    }).compileComponents();

    fixture = TestBed.createComponent(InspeccionCuadrilla);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
